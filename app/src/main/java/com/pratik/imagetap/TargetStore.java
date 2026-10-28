package com.pratik.imagetap;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.*;
import java.util.*;
final class TargetStore {
    static File folder(Context c) { return PresetStore.folder(c,PresetStore.active(c)); }
    static List<File> files(Context c) {
        return PresetStore.files(folder(c));
    }
    static File save(Context c, Bitmap b) throws IOException {
        if(files(c).size()>=500) throw new IOException("Maximum 500 targets. Delete one first.");
        File f=new File(folder(c),"Target_"+System.currentTimeMillis()+"_"+UUID.randomUUID()+".png");
        File temp=new File(folder(c),f.getName()+".tmp");
        try{try(FileOutputStream o=new FileOutputStream(temp)){if(!b.compress(Bitmap.CompressFormat.PNG,100,o))throw new IOException("Could not save image");o.getFD().sync();}if(!temp.renameTo(f))throw new IOException("Could not finalize image");return f;}catch(IOException|RuntimeException e){temp.delete();throw new IOException("Could not save image",e);}

    }
    static List<File> activeFiles(Context c){List<File> files=files(c);files.removeIf(f->!TargetOptions.load(c,f).enabled);return files;}
    static List<File> allActiveFiles(Context c){List<File> out=new ArrayList<>();for(ActionType a:ActionType.values())out.addAll(activeFiles(ActionType.scope(c,a)));return out;}
    static ActionType actionOf(Context c,File file){for(ActionType a:ActionType.values()){Context scope=ActionType.scope(c,a);if(folder(scope).equals(file.getParentFile()))return a;}return ActionType.TAP;}
    static Bitmap thumbnail(File f) {
        BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;BitmapFactory.decodeFile(f.getAbsolutePath(),o);
        o.inSampleSize=1;while(Math.max(o.outWidth,o.outHeight)/o.inSampleSize>180)o.inSampleSize*=2;
        o.inJustDecodeBounds=false;return BitmapFactory.decodeFile(f.getAbsolutePath(),o);
    }
    static Bitmap load(File f) { return BitmapFactory.decodeFile(f.getAbsolutePath()); }
    static File backupRoot(Context c){File f=new File(c.getFilesDir(),PresetStore.active(c)==0&&folder(c).getName().equals("targets")?"merge-backups":"merge-backups-"+folder(c).getName());f.mkdirs();return f;}
    static List<File> backups(Context c){return MergeStorage.backups(backupRoot(c));}
    static void merge(Context c,List<File> group,File keep)throws IOException{MergeStorage.merge(backupRoot(c),group,keep);}
    static void undoMerge(Context c)throws IOException{MergeStorage.undoMerge(folder(c),backupRoot(c));}
}
