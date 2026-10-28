package com.pratik.imagetap;
import java.util.*;
public class DuplicateSimilarityTest {
    static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
    static DuplicateSimilarity.Signature flat(int color,String hash){int[] p=new int[64*64];Arrays.fill(p,0xff000000|(color<<16)|(color<<8)|color);return new DuplicateSimilarity.Signature(100,100,p,hash);}
    public static void main(String[] args){
        DuplicateSimilarity.Signature a=flat(100,"same"),same=flat(100,"same"),near=flat(102,"near"),far=flat(104,"far");
        require(DuplicateSimilarity.score(a,same)==100,"Exact pixel digest is 100%");require(DuplicateSimilarity.score(a,near)>=99&&DuplicateSimilarity.score(a,near)<100,"Near duplicate qualifies at 99, not 100");
        require(DuplicateSimilarity.score(a,far)<99,"Larger color difference rejected");
        List<List<Integer>> groups=DuplicateSimilarity.groups(Arrays.asList(a,near,far),99);require(groups.size()==1&&groups.get(0).size()==2,"Do not merge transitive near-duplicate chains");
        require(DuplicateSimilarity.groups(Arrays.asList(a,near),100).isEmpty(),"Exact-only mode excludes near copies");
        require(DuplicateSimilarity.groups(Arrays.asList(a,same,near),100).get(0).size()==2,"Exact duplicates grouped");
        DuplicateSimilarity.Signature looser=flat(140,"looser");require(DuplicateSimilarity.groups(Arrays.asList(a,looser),80).size()==1,"80% accepts a looser suggestion");require(DuplicateSimilarity.groups(Arrays.asList(a,looser),99).isEmpty(),"99% rejects that same suggestion");
        int[] pattern=new int[64*64];Random r=new Random(5);for(int i=0;i<pattern.length;i++)pattern[i]=0xff000000|r.nextInt(0x1000000);
        DuplicateSimilarity.Signature distinct=new DuplicateSimilarity.Signature(100,100,pattern,"different");require(DuplicateSimilarity.score(a,distinct)<99,"Unrelated structure rejected");
        require(DuplicateSimilarity.score(a,new DuplicateSimilarity.Signature(200,100,a.pixels,"wide"))==0,"Different aspect ratio not merged");
        System.out.println("PASS duplicate matching: exact/99% modes, unrelated/aspect rejection, complete-link grouping without transitive overmerge");
    }
}
