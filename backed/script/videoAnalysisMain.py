# coding=utf-8
import sys

import cv2
import numpy as np
import warnings
import matplotlib
import json
from matplotlib import pyplot as plt

matplotlib.use('TkAgg')
plt.rcParams['font.family'] = 'Microsoft YaHei'
plt.rcParams['axes.unicode_minus'] = False  # 解决负号显示问题
# warnings.filterwarnings('ignore')
warnings.filterwarnings("ignore", message="iCCP: cHRM chunk does not match sRGB")

"""
2688*1520
"""
# 设置遮挡区域，左上、右下坐标
cover_area = {
    'datetime': [(67, 79), (893, 141)],  # 时间日期
    'number': [(1951, 1350), (2245, 1394)],  # 图像编号
}
# 图片分辨率
img_width = 2688
img_height = 1520

# 设置轮廓面积阈值，根据实际情况调整
# 噪点过滤条件，机舱内可能存在轻微抖动，可通过此参数调整
contour_area_threshold = 100
# 设置窗口大小
window_width = int(img_width * 0.3)
window_height = int(img_height * 0.3)


def display_img(image, title):
    # 调整图像大小
    resized_image = cv2.resize(image, (window_width, window_height))

    # 创建窗口并设置大小
    cv2.namedWindow(title, cv2.WINDOW_NORMAL)
    cv2.resizeWindow(title, window_width, window_height)

    # 显示图像
    cv2.imshow(title, resized_image)


def image_brightness(image):
    # for i in range(-30, 31):
    #     brightened_image = cv2.add(image, i)
    #     # 确保像素值不会小于 0
    #     brightened_image = np.maximum(brightened_image, 0)
    #
    #     # 确保像素值不会大于 255
    #     brightened_image = np.minimum(brightened_image, 255)
    #     yield i, brightened_image
    yield 1, image


def image_diff(background_image, current_image, contour_area_percentage_threshold=1, total_contour_area_threshold=3000):
    """

    :param background_image: 背景图片地址，或图片对象
    :param current_image: 当前图片地址，或图片对象
    :param contour_area_percentage_threshold: 判断是否存在异物阈值（百分比）
    :param total_contour_area_threshold: 判断是否存在异物阈值（面积）
    :return:
    """
    diff_resp = {'result': False, 'diff_value': 0}
    # 加载固定背景图像
    if isinstance(background_image, str) and isinstance(current_image, str):
        background_image = cv2.imread(background_image)
        current_image = cv2.imread(current_image)
    current_image = cv2.resize(current_image, (background_image.shape[1], background_image.shape[0]))
    for area_name, area in cover_area.items():
        left_up, right_down = area
        background_image = cv2.rectangle(background_image, left_up, right_down, (0, 0, 0), -1)
        current_image = cv2.rectangle(current_image, left_up, right_down, (0, 0, 0), -1)

    # 如果光线随时间变化，对画面的影响较为均匀，可以对画面进行整体的亮度调整，然后找到差异最小的情况
    # 计算绝对差异
    for i, img in image_brightness(current_image):
        diff = cv2.absdiff(background_image, img)
#         display_img(diff, "diff_image")

        # 将差异转换为灰度图像
        gray_diff = cv2.cvtColor(diff, cv2.COLOR_BGR2GRAY)
#         display_img(gray_diff, "gray_diff_image")
        # 使用阈值去除小噪声
        _, thresh = cv2.threshold(gray_diff, 30, 255, cv2.THRESH_BINARY)

        # if i==-30 or i==30:
        # display_img(background_image, "background_image")
        # display_img(img, "add_image")
        # display_img(gray_diff, "gray_diff")
        #
        # cv2.waitKey(0)
        # cv2.destroyAllWindows()

        # 对二值化图像进行形态学操作以减少噪声
        kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))
        thresh = cv2.morphologyEx(thresh, cv2.MORPH_OPEN, kernel)
#         display_img(thresh, "thresh")

        # 寻找轮廓
        contours, _ = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

        # 初始化总轮廓面积
        total_contour_area = 0
        total_area = background_image.shape[0] * background_image.shape[1]
        # 画出轮廓
        for contour in contours:
            # 可选：通过面积过滤小物体
            if cv2.contourArea(contour) > contour_area_threshold:
                # 获取轮廓的边界框
                (x, y, w, h) = cv2.boundingRect(contour)
                # 画出轮廓
                cv2.rectangle(current_image, (x, y), (x + w, y + h), (0, 255, 0), 2)
                # 累加轮廓面积
                total_contour_area += cv2.contourArea(contour)

        # 判断是否存在异物，基于绝对值判断
        # if total_contour_area > total_contour_area_threshold:
        #     print("检测到异物！")
        #     diff_resp["result"] = "1"
        # diff_resp["diff_value"] = round(total_contour_area, 2)
        # else:
        #     print("未检测到异物")
        # print(total_contour_area,total_area)
        # # 基于百分比阈值判断
        contour_area_percentage = (total_contour_area / total_area) * 100
        if contour_area_percentage > contour_area_percentage_threshold:
            # print(f"检测到异物！轮廓面积占图像面积的 {contour_area_percentage:.2f}%,补充亮度值 {i}")
            diff_resp["result"] = True
            diff_resp["diff_value"] = round(contour_area_percentage, 2)
        # else:
        # print(f"未检测到异物。轮廓面积占图像面积的 {contour_area_percentage:.2f}%,补充亮度值 {i}")
        # print(f"总轮廓面积: {total_contour_area}, 图像总面积: {total_area}")
        # print(f"轮廓面积百分比: {contour_area_percentage:.2f}%")

    # 显示原始帧和差异图像
    # 等待用户按键按下
    cv2.waitKey(0)
    # 释放关闭窗口
    cv2.destroyAllWindows()
    return diff_resp


if __name__ == "__main__":
    # bg_image_path = '../../../StaticFiles/jcqq2.png'
    # cur_image_path = '../../../StaticFiles/jcqq3.png'
    bg_image_path = sys.argv[1]
    cur_image_path = sys.argv[2]
    print(json.dumps(image_diff(bg_image_path, cur_image_path)))
