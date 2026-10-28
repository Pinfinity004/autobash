package com.pratik.imagetap;
import java.util.*;
public class RegionRulesTest {
    static void require(boolean b,String s){if(!b)throw new AssertionError(s);}
    public static void main(String[] args){
        List<RegionRules.Region> regions=Arrays.asList(new RegionRules.Region(0,0,1,.4f,75),new RegionRules.Region(0,.4f,1,.8f,100));
        byte[] map=RegionRules.accuracyMap(100,100,96,regions);
        require(map[10*100+50]==75,"Top lower accuracy overrides global 96");require(map[60*100+50]==100,"Middle strict accuracy");require(map[90*100+50]==96,"Unmarked area fallback");
        byte[] overlap=RegionRules.accuracyMap(100,100,99,Arrays.asList(new RegionRules.Region(0,0,1,1,75),new RegionRules.Region(.2f,.2f,.8f,.8f,85),new RegionRules.Region(.4f,.4f,.6f,.6f,100)));
        require(overlap[0]==75,"Regional 75 overrides global 99");require(overlap[30*100+30]==85,"Overlap uses highest regional accuracy");require(overlap[50*100+50]==100,"Strict overlap wins");
        require(RegionRules.accuracyMap(100,100,96,Collections.emptyList())==null,"No-region compatibility");
        RegionRules.Region ignored=new RegionRules.Region(.4f,.4f,.6f,.6f,90,true),strict=new RegionRules.Region(0,0,1,1,100);
        byte[] a=RegionRules.accuracyMap(100,100,96,Arrays.asList(ignored,strict)),b=RegionRules.accuracyMap(100,100,96,Arrays.asList(strict,ignored));
        require(a[50*100+50]==-1&&b[50*100+50]==-1,"Exclusion wins regardless of drawing order");require(a[0]==100,"Outside exclusion retains normal accuracy");
        byte[] larger=RegionRules.accuracyMap(200,200,96,regions);require(larger[120*200+100]==100,"Regions scale with screen dimensions");
        System.out.println("PASS regions: independent accuracy, fallback, overlap, exclusion priority, screen-relative positions");
    }
}
