package com.pratik.imagetap;
import android.util.AtomicFile;
import java.io.*;import java.nio.charset.StandardCharsets;import java.util.*;
/** Display order belongs to the preset directory; image identities never change. */
final class TargetOrder {
 static List<File> files(File folder){
  File[] raw=folder.listFiles((d,n)->n.endsWith(".png"));List<File> files=new ArrayList<>();if(raw!=null)Collections.addAll(files,raw);
  Map<String,Integer> rank=new HashMap<>();try(BufferedReader reader=new BufferedReader(new InputStreamReader(new AtomicFile(new File(folder,"order.txt")).openRead(),StandardCharsets.UTF_8))){String name;while((name=reader.readLine())!=null)if(!rank.containsKey(name))rank.put(name,rank.size());}catch(IOException ignored){}
  files.sort(Comparator.<File>comparingInt(f->rank.getOrDefault(f.getName(),Integer.MAX_VALUE)).thenComparing(File::getName));return files;
 }
 static void save(File folder,List<File> files)throws IOException{
  Set<String> names=new HashSet<>();StringBuilder text=new StringBuilder();for(File f:files){if(!folder.equals(f.getParentFile())||!f.isFile()||!f.getName().endsWith(".png")||!names.add(f.getName()))throw new IOException("The preset changed. Try again.");text.append(f.getName()).append('\n');}
  AtomicFile file=new AtomicFile(new File(folder,"order.txt"));FileOutputStream out=null;try{out=file.startWrite();out.write(text.toString().getBytes(StandardCharsets.UTF_8));file.finishWrite(out);}catch(IOException e){if(out!=null)file.failWrite(out);throw e;}
 }
 static synchronized void move(File folder,File from,int position)throws IOException{
  List<File> files=files(folder);if(!files.remove(from)||position<0||position>files.size())throw new IOException("The preset changed. Try again.");files.add(position,from);save(folder,files);
 }
}
