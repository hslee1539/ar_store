#include <jni.h>
#include <vector>
#include <opencv2/opencv.hpp>

using namespace cv;

bool findSameAngle(std::vector<Point2f> approx, float errorRate, long minR);
bool getBigRect(std::vector<Point2f> approx);
bool isNeedRecognition(float errorRate);


struct TmpList{
    std::vector<Point2f> rect;
    String str;
};
int listIndex = 0;
unsigned long listIndex_max = 2;
auto qrCodeDetector = QRCodeDetector();
std::vector<struct TmpList> currentList;
std::vector<struct TmpList> previousList;


auto gray = Mat();
auto tmp1 = Mat();

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_cvtest03_CvCameraViewListener_qrRead(JNIEnv *env, jobject instance,
                                                      jlong matAddressInput, jlong matAdressBox) {
    Mat &input = *(Mat*)matAddressInput;
    Mat2f &box = *(Mat2f*)matAdressBox;
    std::vector<Point2f> approx;
    std::vector<std::vector<cv::Point>> contours;
    String str;
    Mat bbox;
    Mat rectifiedImage;

    // RGB인지 BGR인지 확인되지 않음

    cvtColor(input, gray, COLOR_RGBA2GRAY);
    adaptiveThreshold(gray, tmp1, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 31, -20);
    //bitwise_not(tmp1, tmp1); // 이렇게 되는지 검증이 필요함
    findContours(tmp1, contours,RETR_LIST, CHAIN_APPROX_SIMPLE);
    for (const auto &contour : contours) {
        approxPolyDP(Mat(contour), approx, arcLength(Mat(contour), true)*0.02F, true); // 0.02 또는 0.07
        if(approx.size() == 4) {
            if (findSameAngle(approx, 0.15F, 33)) {
                if(getBigRect(approx)) {
                    if (isNeedRecognition(0.1F)) {
                        Mat frame = gray(
                                Range(static_cast<int>(currentList[listIndex].rect[0].y),
                                      static_cast<int>(currentList[listIndex].rect[1].y)),
                                Range(static_cast<int>(currentList[listIndex].rect[0].x),
                                      static_cast<int>(currentList[listIndex].rect[1].x)));
                        String tmpStr = qrCodeDetector.detectAndDecode(frame, bbox, rectifiedImage);
                        if (tmpStr.length() > 0) {
                            // 띄어 쓰기로 split 해서 쓰자!

                            currentList[listIndex].str = tmpStr;
                            listIndex++;
                        }
                    }
                }
            }
        }
    }
    for(int i = 0; i < listIndex; i ++){
        //rectangle(input, currentList[i].rect[0], currentList[i].rect[1], red, 2);
        previousList[i].rect[0].x = currentList[i].rect[0].x;
        previousList[i].rect[1].x = currentList[i].rect[1].x;
        previousList[i].rect[0].y = currentList[i].rect[0].y;
        previousList[i].rect[1].y = currentList[i].rect[1].y;
        previousList[i].str = currentList[i].str;
        str += currentList[i].str + ' ';
    }
    // 부하가 부담되면 여기를 생략 가능
    for(int i = listIndex; i < listIndex_max; i++){
        currentList[i].rect[0].x = -1;
        currentList[i].rect[0].y = -1;
        currentList[i].rect[1].x = -1;
        currentList[i].rect[1].y = -1;
        previousList[i].rect[0].x = -1;
        previousList[i].rect[0].y = -1;
        previousList[i].rect[1].x = -1;
        previousList[i].rect[1].y = -1;
        currentList[i].str = "";
        previousList[i].str = "";
    }
    auto data = box.ptr<Point2f>();
    for (auto &item : currentList) {
        data->x = item.rect[0].x;
        data->y = item.rect[0].y;
        data++;
        data->x = item.rect[1].x;
        data->y = item.rect[1].y;
        data++;
    }

    listIndex = 0;
    return env->NewStringUTF(str.c_str());
}

//사각형에 최적화 함
bool findSameAngle(std::vector<Point2f> approx, float errorRate, long minR){
    float meanX = (approx[0].x + approx[1].x + approx[2].x + approx[3].x) / 4;
    float meanY = (approx[0].y + approx[1].y + approx[2].y + approx[3].y) / 4;
    float r1 = sqrt((approx[0].x - meanX) * (approx[0].x - meanX) + (approx[0].y - meanY) * (approx[0].y - meanY));
    float r2 = sqrt((approx[1].x - meanX) * (approx[1].x - meanX) + (approx[1].y - meanY) * (approx[1].y - meanY));
    float r3 = sqrt((approx[2].x - meanX) * (approx[2].x - meanX) + (approx[2].y - meanY) * (approx[2].y - meanY));
    float r4 = sqrt((approx[3].x - meanX) * (approx[3].x - meanX) + (approx[3].y - meanY) * (approx[3].y - meanY));
    float meanR = (r1 + r2 + r3 + r4) / 4;

    if( meanR > minR){
        if((1 + errorRate) < (r1 / meanR) or (r1 / meanR) < (1 - errorRate))
            return false;
        else if((1 + errorRate) < (r2 / meanR) or (r2 / meanR) < (1 - errorRate))
            return false;
        else if((1 + errorRate) < (r3 / meanR) or (r3 / meanR) < (1 - errorRate))
            return false;
        else
            return !((1 + errorRate) < (r4 / meanR) or (r4 / meanR) < (1 - errorRate));
    }
    return false;
}


// 4각형에 최적화 함. 다각형알고리즘을 바꾸려면 for문을 사용할 것.
bool getBigRect(std::vector<Point2f> approx){
    if(listIndex < listIndex_max) {
        float minX = approx[0].x;
        float minY = approx[0].y;
        float maxX = approx[0].x;
        float maxY = approx[0].y;
        if (minX > approx[1].x)
            minX = approx[1].x;
        else if (maxX < approx[1].x)
            maxX = approx[1].x;
        if (minX > approx[2].x)
            minX = approx[2].x;
        else if (maxX < approx[2].x)
            maxX = approx[2].x;
        if (minX > approx[3].x)
            minX = approx[3].x;
        else if (maxX < approx[3].x)
            maxX = approx[3].x;
        if (minY > approx[1].y)
            minY = approx[1].y;
        else if (maxY < approx[1].y)
            maxY = approx[1].y;
        if (minY > approx[2].y)
            minY = approx[2].y;
        else if (maxY < approx[2].y)
            maxY = approx[2].y;
        if (minY > approx[3].y)
            minY = approx[3].y;
        else if (maxY < approx[3].y)
            maxY = approx[3].y;
        currentList[listIndex].rect[0].x = minX;
        currentList[listIndex].rect[0].y = minY;
        currentList[listIndex].rect[1].x = maxX;
        currentList[listIndex].rect[1].y = maxY;
        return true;
    }
    return false;
}

bool isNeedRecognition(float errorRate){
    float currentMeanX = (currentList[listIndex].rect[0].x + currentList[listIndex].rect[1].x) / 2;
    float currentMeanY = (currentList[listIndex].rect[0].y + currentList[listIndex].rect[1].y) / 2;
    float previousMeanX = 0;
    float previousMeanY = 0;

    for(int i = 0; i < listIndex; i ++){
        previousMeanX = (currentList[i].rect[0].x + currentList[i].rect[1].x) / 2;
        previousMeanY = (currentList[i].rect[0].y + currentList[i].rect[1].y) / 2;
        previousMeanX /= currentMeanX;
        previousMeanY /= currentMeanY;
        if((1 - errorRate) < previousMeanX and previousMeanX < (1 + errorRate) and (1 - errorRate) < previousMeanY and previousMeanY < (1 + errorRate)){
            return false;
        }
    }

    for(int i = 0; i < listIndex_max; i ++){
        previousMeanX = (previousList[i].rect[0].x + previousList[i].rect[1].x) / 2;
        previousMeanY = (previousList[i].rect[0].y + previousList[i].rect[1].y) / 2;
        previousMeanX /= currentMeanX;
        previousMeanY /= currentMeanY;
        if((1 - errorRate) < previousMeanX and previousMeanX < (1 + errorRate) and (1 - errorRate) < previousMeanY and previousMeanY < (1 + errorRate)){
            currentList[listIndex].str = previousList[i].str;
            listIndex++;
            return false;
        }
    }
    return true;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_cvtest03_CvCameraViewListener_startCamera(JNIEnv *env, jobject instance) {
    currentList = std::vector<struct TmpList>(listIndex_max);
    previousList = std::vector<struct TmpList>(listIndex_max);
    for(int i = 0; i < listIndex_max; i ++){
        currentList[i].rect = std::vector<Point2f>(2);
        currentList[i].rect[0].x = -1;
        currentList[i].rect[0].y = -1;
        currentList[i].rect[1].x = -1;
        currentList[i].rect[1].y = -1;
        previousList[i].rect = std::vector<Point2f>(2);
        previousList[i].rect[0].x = -1;
        previousList[i].rect[0].y = -1;
        previousList[i].rect[1].x = -1;
        previousList[i].rect[1].y = -1;
        currentList[i].str = "";
        previousList[i].str = "";
    }
    // TODO

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_cvtest03_CvCameraViewListener_stopCamera(JNIEnv *env, jobject instance) {

    // TODO

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_cvtest03_CvCameraViewListener_startCamera__J(JNIEnv *env, jobject instance,
                                                              jlong number) {
    currentList = std::vector<struct TmpList>(listIndex_max);
    previousList = std::vector<struct TmpList>(listIndex_max);
    listIndex_max = static_cast<unsigned long>(number);
    for(int i = 0; i < listIndex_max; i ++){
        currentList[i].rect = std::vector<Point2f>(2);
        currentList[i].rect[0].x = -1;
        currentList[i].rect[0].y = -1;
        currentList[i].rect[1].x = -1;
        currentList[i].rect[1].y = -1;
        previousList[i].rect = std::vector<Point2f>(2);
        previousList[i].rect[0].x = -1;
        previousList[i].rect[0].y = -1;
        previousList[i].rect[1].x = -1;
        previousList[i].rect[1].y = -1;
        currentList[i].str = "";
        previousList[i].str = "";
    }
    // TODO

}