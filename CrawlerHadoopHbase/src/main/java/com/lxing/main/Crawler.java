package com.lxing.main;

import com.lxing.fetch.FetchDriver;
import com.lxing.index.IndexDriver;
import com.lxing.inject.InjectDriver;
import com.lxing.optimize.OptimizerDriver;
import com.lxing.parse.ParserArticleDriver;
import com.lxing.parse.ParserUrlDriver;
import com.lxing.util.HbaseUtil;
import com.lxing.util.LuceneUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.nio.file.Paths;

public class Crawler {
    
    public static Logger logger = LoggerFactory.getLogger(Crawler.class);
    
    private InjectDriver injectDriver;//注入驱动
    
    private FetchDriver fetchDriver;//爬取驱动
    
    private ParserUrlDriver parserUrlDriver;//解析URL驱动
    
    private OptimizerDriver optimizerDriver;//去重url驱动
    
    private ParserArticleDriver parserArticleDriver;//解析文章驱动
    
    public Crawler() {
        injectDriver = new InjectDriver();
        fetchDriver = new FetchDriver();
        parserUrlDriver = new ParserUrlDriver();
        optimizerDriver = new OptimizerDriver();
        parserArticleDriver = new ParserArticleDriver();
    }
    
    public static void main(String[] args) throws Exception {
        Configuration conf = HBaseConfiguration.create();
        conf.addResource("app-config.xml");
        conf.addResource("hbase-site.xml");
        Crawler crawler = new Crawler();
        String depthString = conf.get("crawler.depth");
        if(depthString.equals("")||depthString==null){
            System.out.println("请在app-config.xml中配置crawler.depth，即爬虫深度");
        }
        int depth=Integer.valueOf(depthString);
        // 第一步： 注入待抓取的网页url
        int in = crawler.injectDriver.injectFromLocal(conf);
        if (in == 0) {
            logger.error("注入待抓取的url失败！！");
            return;
        }
        // 第二步：循环抓取网页并解析网页上的url，根据正则表达式获取需要的url，并进行去重处理
        for (int i = 1; i <= depth; i++) {
            logger.info("第 " + i + " 层:开始执行FetchDriver,下载页面");
            int fetchCode = ToolRunner.run(crawler.fetchDriver, args);
            if (fetchCode == 0) {
                logger.error("第" + i + "次抓取网页失败！！");
                return;
            }
            logger.info("第 " + i + " 层:开始执行parserUrlDriver,分析页面提取URL");
            int parserUrlCode =
                              ToolRunner.run(crawler.parserUrlDriver, args);
            if (parserUrlCode == 0) {
                logger.error("第" + i + "次解析网页url失败！！");
                return;
            }
            logger.info("第 " + i + " 层:开始执行OptimizerDriver,优化URL");
            int optimizerCode =
                              ToolRunner.run(crawler.optimizerDriver, args);
            if (optimizerCode == 0) {
                logger.error("第" + i + "次优化网页url失败！！");
                return;
            }
            logger.info("第 " + i + " 层：抓取完毕");
        }
        // 第三步：解析已抓取的网页信息，获取文章内容并存储
        int parserArticleCode = ToolRunner.run(crawler.parserArticleDriver,
                                               args);
        if (parserArticleCode == 0) {
            logger.error("解析网页文章信息失败！！！");
            return;
        }

    }
}
