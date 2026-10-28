package com.pratik.imagetap;
import java.io.*;
import java.nio.file.*;
import java.util.*;
public class MergeStorageTest {
    static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
    public static void main(String[] args)throws Exception{
        Path root=Files.createTempDirectory("imagetap-merge-test-");File target=root.resolve("targets").toFile(),backup=root.resolve("backups").toFile();target.mkdir();backup.mkdir();
        File a=new File(target,"a.png"),b=new File(target,"b.png"),c=new File(target,"c.png");Files.write(a.toPath(),new byte[]{1,2,3});Files.write(b.toPath(),new byte[]{4,5,6});Files.write(c.toPath(),new byte[]{7,8,9});
        MergeStorage.merge(backup,Arrays.asList(a,b,c),b);require(b.isFile()&&!a.exists()&&!c.exists(),"Keep selected representative only");require(MergeStorage.backups(backup).size()==1,"Backup batch exists");require(Arrays.equals(Files.readAllBytes(b.toPath()),new byte[]{4,5,6}),"Representative remains unchanged");
        MergeStorage.undoMerge(target,backup);require(a.isFile()&&b.isFile()&&c.isFile(),"Undo restores every entry");require(Arrays.equals(Files.readAllBytes(a.toPath()),new byte[]{1,2,3})&&Arrays.equals(Files.readAllBytes(c.toPath()),new byte[]{7,8,9}),"Restored bytes unchanged");
        MergeStorage.merge(backup,Arrays.asList(a,b),b);Files.write(a.toPath(),new byte[]{42});boolean conflict=false;try{MergeStorage.undoMerge(target,backup);}catch(IOException e){conflict=true;}require(conflict,"Restore does not overwrite an existing entry");require(Files.readAllBytes(a.toPath())[0]==42&&!MergeStorage.backups(backup).isEmpty(),"Conflict retains current file and backup");
        try(java.util.stream.Stream<Path> paths=Files.walk(root)){paths.sorted(Comparator.reverseOrder()).forEach(p->{try{Files.delete(p);}catch(IOException e){throw new UncheckedIOException(e);}});}
        System.out.println("PASS merge storage: selected copy retained, recoverable backups, exact undo, no overwrite on restore conflicts");
    }
}
