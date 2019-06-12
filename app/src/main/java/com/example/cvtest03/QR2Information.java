package com.example.cvtest03;

public class QR2Information {
    public GoodsData[] data;
    public QR2Information(){
        this.data = new GoodsData[4];
        this.data[0] = new GoodsData("9999", "신라면", 950, "식품", "농심", "대한민국의 대표 라면입니다.");
        this.data[1] = new GoodsData("10", "무선마우스", 7500, "전자제품", "로지텍", "인체공학적으로 설계된 무선 마우스입니다.");
        this.data[2] = new GoodsData("20", "153펜", 500, "사무용품", "모나미", "무난한 모나미 153 볼펜입니다.");
        this.data[3] = new GoodsData("", "", 0, "", "", "");
    }
    // @params mode 0 : 간단히, 1: 자세히
    public String getInformation(String qr, int mode){
        GoodsData choice;
        if(this.data[0].equals(qr)){
            choice = this.data[0];
        }
        else if(this.data[1].equals(qr)){
            choice = this.data[1];
        }
        else if(this.data[2].equals(qr)){
            choice = this.data[2];
        }
        else{
            choice = this.data[3];
        }
        if(mode == 0){
            return choice.summary;
        }
        return choice.body;
    }
    public String getInformStringScale(String qr, int scale){
        if(scale > 400){
            return getInformation(qr, 1);
        }
        return getInformation(qr, 0);
    }
}
