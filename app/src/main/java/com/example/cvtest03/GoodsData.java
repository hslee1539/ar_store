package com.example.cvtest03;

public class GoodsData {
    public static String newLine = "\n";
    public String qrcode;
    public String summary;
    public String body;
    public GoodsData(String qrcode, String name, int price, String tag, String company,  String extra){
        this.qrcode = qrcode;
        this.summary = name + newLine + price + "원";
        this.body = "제품명: " + name + newLine + "가격: "+ price + "원" + newLine + "항목: "+ tag + newLine + "회사: " + company + newLine + "추가정보:" + newLine + extra;
    }
    //@Override
    public boolean equals(GoodsData goodsData){
        return this.qrcode.equals(goodsData.qrcode);
    }
    @Override
    public boolean equals(Object qrcode){
        return this.qrcode.equals(qrcode);
    }
}
