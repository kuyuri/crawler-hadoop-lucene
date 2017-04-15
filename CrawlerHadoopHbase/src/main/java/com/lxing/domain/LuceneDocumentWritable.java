package com.lxing.domain;

import org.apache.hadoop.io.Writable;
import org.apache.lucene.document.Document;

import java.io.DataInput;
import java.io.DataOutput;

/**
 * @Description： 存放lucene Document的writable类 @Author： lxing @Date： 16:40
 * 2017/4/08
 * 
 * @modified By：
 */
public class LuceneDocumentWritable implements Writable {
    protected Document doc;
    
    public LuceneDocumentWritable(Document doc) {
        this.doc = doc;
    }
    
    public Document get() {
        return doc;
    }
    
    public void readFields(DataInput in) {
        
    }
    
    public void write(DataOutput out) {
    }
}
