package com.attempt.npoc.entity;

import lombok.Data;

@Data
public class Regexp {
    private String expr;
//    private Prog prog;
//    private onePassProg onepass;
    private int numSubexp;
    private int maxBitStateLen;
    private String[] subexpNames;
    private String prefix;
    private byte[] prefixBytes ;
    private int prefixRune;
    private  long prefixEnd;
    private int mPool;
    private int matchCap;
    private boolean prefixComplete ;
//    private EmptyOp cond;
    private int minInputLen;
}
