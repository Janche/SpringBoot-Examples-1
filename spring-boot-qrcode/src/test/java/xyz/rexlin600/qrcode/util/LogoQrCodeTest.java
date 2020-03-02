package xyz.rexlin600.qrcode.util;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * @description
 * @auther hekunlin
 * @create 2020-03-02 14:24
 */
public class LogoQrCodeTest {

    private static final File LOGO_FILE = new File("C:\\Users\\hekunlin\\Pictures\\golang.jpg");
    private static final String LOCAL_FILE_PATH = "C:\\Users\\hekunlin\\Pictures\\QRCode\\logo.png";
    private static BufferedImage bufferedImage = null;

    /**
     * 初始化 qrcode
     */
    @Before
    public void getQrCode() {
        bufferedImage = QrCodeUtil.simpleQrCode("this is a logo QRCode");
    }

    /**
     * 生成带 logo 二维码
     */
    @SneakyThrows
    @Test
    public void logoQrCode() {
        BufferedImage bufferedImage = QrCodeUtil.logoQrCode(LogoQrCodeTest.bufferedImage, LOGO_FILE);
        QrCodeUtil.write2File(bufferedImage, "png", LOCAL_FILE_PATH);
    }

    /**
     * 生成带 logo 二维码
     */
    @SneakyThrows
    @Test
    public void testLogoQrCode() {
        URL url = new URL("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1583127516250&di=3cf42d0daf3c7df9154f277119e6cc8c&imgtype=0&src=http%3A%2F%2Fa0.att.hudong.com%2F78%2F52%2F01200000123847134434529793168.jpg");
        BufferedImage bufferedImage = QrCodeUtil.logoQrCode(LogoQrCodeTest.bufferedImage, url);
        QrCodeUtil.write2File(bufferedImage, "png", LOCAL_FILE_PATH);
    }

    /**
     * 省略其他测试 ...
     */
}