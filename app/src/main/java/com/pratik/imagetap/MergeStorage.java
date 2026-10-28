package com.pratik.imagetap;
import java.io.*;
import java.util.*;
final class MergeStorage {
    static List<File> pngFiles(File folder){File[] a=folder.listFiles((d,n)->n.endsWith(".png"));return a==null?new ArrayList<>():Arrays.asList(a);}
    static List<File> backups(File backupRoot){
        File[] folders=backupRoot.listFiles(File::isDirectory);List<File> out=new ArrayList<>();if(folders!=null)for(File f:folders){File[] a=f.listFiles((d,n)->n.endsWith(".png"));if(a!=null&&a.length>0)out.add(f);}
        out.sort(Comparator.comparing(File::getName).reversed());return out;
    }
    static void merge(File backupRoot,List<File> group,File keep)throws IOException{
        if(!group.contains(keep)||!keep.isFile())throw new IOException("The selected image is no longer available.");
        File archive=new File(backupRoot,System.currentTimeMillis()+"_"+UUID.randomUUID());if(!archive.mkdirs())throw new IOException("Could not create merge backup.");
        List<File> moved=new ArrayList<>();try{
            for(File f:group)if(!f.equals(keep)){if(!f.isFile()||!f.renameTo(new File(archive,f.getName())))throw new IOException("Could not back up an image. Merge was cancelled.");moved.add(f);}
        }catch(IOException e){for(File f:moved)if(!new File(archive,f.getName()).renameTo(f))throw new IOException("Merge failed; some images remain in the recoverable merge backup.",e);throw e;}
    }
    static void undoMerge(File targets,File backupRoot)throws IOException{
        List<File> batches=backups(backupRoot);if(batches.isEmpty())throw new IOException("There is no merge to undo.");
        File[] items=batches.get(0).listFiles((d,n)->n.endsWith(".png"));if(items==null)throw new IOException("Backup unavailable.");
        if(pngFiles(targets).size()+items.length>500)throw new IOException("Make space for "+items.length+" images before restoring this merge.");
        for(File f:items)if(new File(targets,f.getName()).exists())throw new IOException("An image with this name already exists; backup kept.");
        List<File> moved=new ArrayList<>();try{for(File f:items){if(!f.renameTo(new File(targets,f.getName())))throw new IOException("Could not restore images.");moved.add(f);}}
        catch(IOException e){for(File f:moved)if(!new File(targets,f.getName()).renameTo(f))throw new IOException("Restore incomplete; original files and backup are retained.",e);throw e;}
    }
}
