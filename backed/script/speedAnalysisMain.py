# coding=utf-8
import json
import os
import sys
import traceback

import numpy as np
import pandas as pd
from scipy.interpolate import interp1d
import warnings
import matplotlib
from matplotlib import pyplot as plt

matplotlib.use('TkAgg')
plt.rcParams['font.family'] = 'Microsoft YaHei'
plt.rcParams['axes.unicode_minus'] = False  # 解决负号显示问题
warnings.filterwarnings('ignore')


def draw_plot(x, y, xlabel=None, ylabel=None, title='', **kwargs):
    plt.plot(x, y)
    if kwargs.get('envelope', False):
        if kwargs.get('upper_value') is not None:
            plt.plot(x, kwargs.get('upper_value'), label='upper_value')
        if kwargs.get('lower_value') is not None:
            plt.plot(x, kwargs.get('lower_value'), label='lower_value')
        if kwargs.get('baseLine') is not None:
            plt.plot(x, kwargs.get('baseLine'), label='baseLine')
        if xlabel:
            plt.xlabel(xlabel)
    plt.ylabel(ylabel)
    plt.legend(loc='upper right')
    plt.title(title)
    plt.show()


def draw_log_plot2(x, y, xlabel=None, ylabel=None, title='', **kwargs):
    plt.plot(x, y.flatten(), color='red')
    from scipy.signal import savgol_filter
    # 使用Savitzky-Golay滤波器进行平滑处理
    window_length = 51  # 窗口长度
    polyorder = 3  # 多项式阶数
    smoothed_y = savgol_filter(y.flatten(), window_length, polyorder)
    # plt.plot(x, smoothed_y, label='Smoothed Data', color='blue')
    plt.xscale('log')
    plt.yscale('log')
    plt.ylabel(ylabel)
    plt.title(title)
    # plt.xlim([0.1, max(x) * 1.01])
    # plt.xlim([0.1, max(np.log10(x))*1.01])
    plt.show()


def draw_log_plot(x, y, x_envelope, y_envelope, xlabel=None, ylabel=None, title='', **kwargs):
    plt.plot(x, y, color='red', linewidth=1)
    plt.plot(x_envelope, y_envelope, color='blue', label='Max Values', linewidth=1)
    plt.ylabel(ylabel)
    plt.title(title)
    # plt.xlim([0.1, max(x) * 1.01])
    # plt.ylim([min(y), max(y) * 1.01])
    plt.show()


class Analysis:
    def __init__(self, sample_interval=0.01):
        self.sample_interval = sample_interval
        self.data = np.array([])
        try:
            envelope_df = pd.read_excel('../envelope.xlsx', engine='openpyxl')
            self.envelope = (np.array(envelope_df['x'].tolist()), np.array(envelope_df['y'].tolist()))
        except:
            self.envelope = None

    def get_data(self, source_data):
        if not isinstance(source_data, np.ndarray):
            self.data = np.array(source_data)
        else:
            self.data = source_data

    def calculation_time(self, data, file_path, is_draw=False, is_envelope=False):
        """
        :param data: 数组
        :param is_draw: 是否绘图
        :param is_envelope: 是否需要包络线
        :return:
        """
        self.get_data(data)
        x_value = np.arange(0, np.size(self.data) * self.sample_interval, self.sample_interval)
        y_value = self.data
        x_label = 'Time [s]'

        # 绘制包络线
        upper_value, lower_value, rms = self.plot_envelope(y_value, 'valid')
        base_line = self.baseline_median_filter(y_value)

        if is_draw:
            draw_plot(x_value, y_value, xlabel=x_label, title='speed-time',
                      **{'envelope': is_envelope, 'upper_value': upper_value, 'lower_value': lower_value,
                         'baseLine': base_line})
        self.write_file(file_path,{'x': list(x_value), 'y': list(y_value)})
        return {'x': x_value, 'y': y_value}

    def calculation_freq(self, data, file_path, is_draw, threshold):
        """
        :param data: 数组
        :param is_draw: 是否绘图
        :param threshold: 报警阈值
        :return:
        """
        self.get_data(data)
        fs = 1 / self.sample_interval
        x_value, y_value = self.ecn_spectra_db(fs)
        x_label = 'Frequency [Hz]'
        # 绘制包络线
        y_value = y_value.flatten()
        x_value = np.log10(x_value)
        y_value = np.log10(y_value.flatten())
        x_value, y_value = self.__array_by_index(x_value, y_value, 0.1)

        if self.envelope is None:
            self.envelope = self.envelope_algorithm(x_value, y_value)
        x_envelope_value, y_envelope_value = self.__array_by_index(*self.envelope, 0.1)
        y_envelope_value = y_envelope_value * (1 - threshold)
        if is_draw:
            draw_log_plot(x_value, y_value, x_envelope_value, y_envelope_value, xlabel=x_label, title='speed-freq')
        result = list(x_value), list(y_value), list(x_envelope_value), list(y_envelope_value)
        self.write_file(file_path, result)
        return x_value, y_value, x_envelope_value, y_envelope_value

    def find_crossings(self, data, file_path, threshold: float = 0.15, is_draw: bool = False) -> dict:
        x1, y1, x2, y2 = self.calculation_freq(data=data,file_path=file_path, is_draw=is_draw, threshold=threshold)
        sign_changes, crossings_x, crossings_y1, crossings_y2 = self.__find_crossings(x1, y1, x2, y2)
        result = {
            'spectrum': {'x': np.nan_to_num(x1, posinf=0, neginf=0).tolist(), 'y': np.nan_to_num(y1, posinf=0, neginf=0).tolist()},  # 频谱图
            'threshold': {'x': np.nan_to_num(x2, posinf=0, neginf=0).tolist(), 'y': np.nan_to_num(y2, posinf=0, neginf=0).tolist()},  # 包络线
            'crossings': {'x': np.nan_to_num(crossings_x, posinf=0, neginf=0).tolist(), 'y1': np.nan_to_num(crossings_y1, posinf=0, neginf=0).tolist(), 'y2': np.nan_to_num(crossings_y2, posinf=0, neginf=0).tolist()},  # 交点位置
            'is_crossings': np.size((sign_changes > 0)) > 0  # 是否有交点
        }
        self.write_file(file_path, result)
        return result

    def ecn_spectra_db(self, fs, k=1):
        """
        计算功率谱密度并返回频率和功率谱密度。
        """
        nfft = 2 ** (np.ceil(np.log2(np.size(self.data) / k)) - 1).astype(int)
        timestep = 1 / fs
        data_use = self.data - np.mean(self.data)
        n_psd_x, fg_x, df, syu, fs, [] = self.ecnpsdpergram(data_use, data_use, timestep, nfft / 4, nfft)
        f = fg_x
        f_db = n_psd_x
        return f, f_db

    def ecnpsdpergram(self, y, u, dt, Nf, N, CHECK=False, linetyp=None):
        """
        计算信号的谱密度
        :param y: 输出信号数组
        :param u: 输入信号数组
        :param dt: 时间步长
        :param Nf: 频率点数
        :param N: 数据点数
        :param CHECK: 是否进行数据检查，默认为False
        :param linetyp: 线条类型，默认为None
        :return: 交叉谱密度,频率轴,频率分辨率,交叉谱密度（完整版）,频率轴（完整版）,保留空数组，用于未实现的功能
        """
        # 设置默认的线条类型
        if linetyp is None:
            linetyp = []
        if len(linetyp) == 0 and CHECK == 0:
            linetyp = 'b'
        # 初始化标志变量
        NfFlag2 = 1
        Nfm1Flag2 = 1
        Nf = int(Nf)
        # 检查 Nf 是否是 2 的幂次方
        if not np.isclose(np.log2(Nf), round(np.log2(Nf))):
            NfFlag2 = 0
        if not np.isclose(np.log2(Nf + 1), round(np.log2(Nf + 1))):
            Nfm1Flag2 = 0
        if not NfFlag2 and not Nfm1Flag2:
            raise ValueError('Nf neither power of 2 nor power of 2 minus 1')
        if Nfm1Flag2:
            Nf = Nf + 1

        # 检查N是否为2的幂且大于2*Nf
        if not np.isclose(np.log2(N), round(np.log2(N))):
            raise ValueError('N no power of 2')
        if N < 2 * Nf:
            raise ValueError('N < 2*Nf')

        # 将输入和输出信号转换为列向量
        u = self.ecnvc2col(u[:N])
        y = self.ecnvc2col(y[:N])

        # 计算频率轴和频率分辨率
        f = np.arange(Nf) / (2 * Nf * dt)
        df = 1 / (2 * Nf * dt)

        # 计算修正后的频率轴
        f4daniel = np.concatenate(([0.5 * df], f[1:] + 0.5 * df))
        # 计算信号的傅里叶变换
        Fx2 = 1 / np.sqrt(N * dt) * np.fft.fft2(y) * dt
        Fx2[1::2] = -Fx2[1::2]
        Fx3 = 1 / np.sqrt(N * dt) * np.fft.fft2(u) * dt
        Fx3[1::2] = -Fx3[1::2]
        # 计算交叉谱密度的临时变量
        stmp = Fx2 * np.conj(Fx3)
        gtmp = np.concatenate(([stmp[0]], 2 * stmp[1:N // 2]))
        dfp = 1 / (dt * N)
        fp = np.arange(N // 2) * dfp
        # 根据N和Nf的关系计算交叉谱密度
        if N > 2 * Nf:
            Np4daniel = (N / 2) / Nf
            gyu = np.zeros((int(Nf), 1))
            Noff4daniel = Np4daniel / 2
            gyu[0, 0] = np.mean(gtmp[:int(Noff4daniel)])
            for k in range(1, Nf):
                gyu[k, 0] = np.mean(gtmp[int(Noff4daniel):int(Noff4daniel + Np4daniel)])
                Noff4daniel += Np4daniel
        else:
            gyu = gtmp
        # 数据检查，如果需要
        if CHECK:
            if N > 2 * Nf:
                gyuCHECK = np.zeros((Nf, 1))
                ixsp = np.max(np.where(fp < f4daniel[0])[0])
                gyuCHECK[0, 0] = np.mean(gtmp[:ixsp + 1])
                for k in range(1, len(f) - 1 + 1):
                    ixsp_start = np.min(np.where(fp >= f4daniel[k - 1])[0])
                    ixsp_end = np.max(np.where(fp < f4daniel[k])[0])
                    if ixsp_start > ixsp_end:
                        raise ValueError(f'k={k}, ixsp = empty')
                    gyuCHECK[k, 0] = np.mean(gtmp[ixsp_start:ixsp_end + 1])
            else:
                gyuCHECK = gtmp
            print(f'max(abs(gyu-gyuCHECK)) = {np.max(np.abs(gyu - gyuCHECK)):10.3e}')

        # 构建完整的频率轴和交叉谱密度
        fg = f
        syu = np.zeros((2 * Nf - 1, 1))
        syu[Nf:2 * Nf - 1, 0] = 0.5 * gyu[1:, 0]
        syu[Nf - 1, 0] = gyu[0, 0]
        syu[:Nf - 1, 0] = 0.5 * np.conj(gyu[-1:0:-1, 0])
        fs = np.zeros((2 * Nf - 1, 1))
        fs[Nf - 1:2 * Nf - 1, 0] = fg
        fs[:Nf - 1, 0] = -fg[-1:0:-1]
        # 如果Nf是2的幂减1，调整频率轴和交叉谱密度
        if Nfm1Flag2:
            fg = fg[1:]
            gyu = gyu[1:]
        return gyu, fg, df, syu, fs, []

    @staticmethod
    def ecnvc2col(vec):
        """
        将行向量转置为列向量（保持矩阵和列向量不变）。
        :param vec: 输入的向量或矩阵。
        :return: 转置后的列向量或原矩阵。
        """
        if vec.ndim == 1 or (vec.shape[0] == 1 and vec.shape[1] > 1):
            colvec = vec.reshape(-1, 1)
        else:
            colvec = vec
        return colvec

    @staticmethod
    def envelope_algorithm(x, y, group_size=20):
        # 分组并求最大值
        num_groups = len(y) // group_size
        max_values = np.array([])
        corresponding_x = np.array([])

        for i in range(num_groups):
            start_idx = i * group_size
            end_idx = (i + 1) * group_size
            group_max = np.max(y[start_idx:end_idx])
            max_values = np.append(max_values, group_max)
            corresponding_x = np.append(corresponding_x, x[start_idx + np.argmax(y[start_idx:end_idx])])
        pd.DataFrame({'x': corresponding_x, 'y': max_values}).to_excel('../envelope.xlsx', index=False)
        return corresponding_x, max_values

    @staticmethod
    def plot_envelope(y_data: np.ndarray, mode: str, window_size: int = 300, rms_window_size: int = 100):
        """计算上下包络线"""
        # 计算移动平均值
        moving_avg = np.convolve(y_data, np.ones(window_size) / window_size, mode=mode)
        # 计算上下包络线
        std_dev = np.std(y_data[:np.size(moving_avg)])
        upper_value = moving_avg + std_dev
        lower_value = moving_avg - std_dev
        value_to_add1 = np.take(upper_value, -1)
        value_to_add2 = np.take(lower_value, -1)
        elements_to_add = np.size(y_data) - np.size(upper_value)
        upper_envelope = np.concatenate((upper_value, np.full(elements_to_add, value_to_add1)))
        lower_envelope = np.concatenate((lower_value, np.full(elements_to_add, value_to_add2)))
        # 求RMS
        interval_data_list = []
        for i in range(0, len(y_data), rms_window_size):
            interval_data = np.take(upper_envelope, i) - np.take(lower_envelope, i)
            interval_data_list.append(interval_data)
        rms = np.sqrt(np.mean(np.array(interval_data_list) ** 2))
        return upper_envelope, lower_envelope, rms

    @staticmethod
    def baseline_median_filter(baseline_data, window_size=600):
        """计算基线"""
        num_windows = np.size(baseline_data) // window_size
        averages = [np.mean(baseline_data[i * window_size:(i + 1) * window_size]) for i in range(num_windows)]
        # 线性插值
        x_baseline = np.linspace(0, np.size(baseline_data), num=num_windows)
        f = interp1d(x_baseline, averages, kind='linear')
        return f(np.arange(np.size(baseline_data)))

    @staticmethod
    def __array_by_index(x_value: np.array, y_value: np.array, value: float):
        """
        根据索引获取数据
        :param index: 索引
        :return: 数据
        """
        indices = np.where(x_value >= value)[0]
        x_value = x_value[indices]
        y_value = y_value[indices]
        return x_value, y_value

    @staticmethod
    def __find_crossings(x1, y1, x2, y2):
        """
        找到两个数组 y1 和 y2 的交叉点，并返回交叉点的索引和对应的 x 值。
        x1,y1为频谱
        x2,y2为阈值包络线
        """
        f = interp1d(x2, y2, kind='linear', fill_value='extrapolate')
        y2_aligned = f(x1)

        diff = y1 - y2_aligned
        sign_changes = np.where(np.diff(np.sign(diff)))[0]
        crossings_x = x1[sign_changes]
        crossings_y1 = y1[sign_changes]
        crossings_y2 = y2_aligned[sign_changes]
        return sign_changes, crossings_x, crossings_y1, crossings_y2

    def write_file(self, path, data):
        try:
            with open(path, 'w', encoding='utf-8') as f:
                f.write(json.dumps(data))
        except:
            print('写入文件失败',type(data),data)
            print(traceback.format_exc())


if __name__ == '__main__':
    draw = False
    analysis = Analysis()

    in_file_path = sys.argv[1]
    # in_file_path = r"C:\esv\windPower\WindAlgorithm\wind_algorithm\apps\ProcessMgr\Speed\new14.txt"
    with open(in_file_path, 'r', encoding='utf-8') as f:
        test_data = json.loads(f.read())
        print("test_data", test_data, type(test_data))

    out_file_path = sys.argv[2]
    # out_file_path = 'output.txt'

    # print(analysis.calculation_time(test_data, out_file_path, is_draw=draw))  # 转速-时间图
    # analysis.calculation_freq(test_data, file_path, draw)
    print(analysis.find_crossings(test_data, out_file_path, is_draw=draw))  # 转速频谱-报警情况

