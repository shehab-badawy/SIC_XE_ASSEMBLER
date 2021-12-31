package com.company;

public class Literal {
    String literalName;
    int literalLoc;
    String literalvalue;
    int literalSize;
    String literalObjectCode;

    public Literal(String literalName,  String literalvalue, int literalSize,String literalObjectCode) {
        this.literalName = literalName;
        this.literalvalue = literalvalue;
        this.literalSize = literalSize;
        this.literalObjectCode = literalObjectCode;
    }
}
