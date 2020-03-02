package xyz.rexlin600.qrcode.util;

import ch.qos.logback.core.util.FileUtil;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import sun.font.FontDesignMetrics;
import xyz.rexlin600.qrcode.entity.QrCodeContent;
import xyz.rexlin600.qrcode.enums.TextPosEnum;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 二维码工具类
 *
 * @author: rexlin600
 * @date: 2020-02-28
 */
@SuppressWarnings("DuplicatedCode")
@Slf4j
public class QrCodeUtil {

    /**
     * 默认参数：二维码长度、二维码宽度、编码格式、纠错等级、固定二维码边框（重要）、前景色-黑、背景色-白
     */
    private final static Integer QR_CODE_HEIGHT = 400;
    private final static Integer QR_CODE_WIDTH = 400;
    private final static String FORMAT = "UTF-8";
    private final static ErrorCorrectionLevel ERR_LEVEL = ErrorCorrectionLevel.M;
    private final static Integer MARGIN = 3;
    private static final int FRONT_COLOR = 0x000000;
    private static final int BACKGROUND_COLOR = 0xFFFFFF;

    // 线程池
    private final static ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNamePrefix("qrcode-pool-%d").build();
    private final static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            50,
            150,
            10,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(),
            namedThreadFactory,
            new ThreadPoolExecutor.AbortPolicy());

    // -----------------------------------------------------------------------------------------------
    // SIMPLE QR CODE
    // -----------------------------------------------------------------------------------------------

    /**
     * 获取简单二维码缓冲图像
     *
     * @param content 二维码内容
     * @return {@link BufferedImage}
     */
    public static BufferedImage simpleQrCode(String content) {
        return simpleQrCode(content, QR_CODE_HEIGHT, QR_CODE_WIDTH, FORMAT, ERR_LEVEL);
    }

    /**
     * 获取简单二维码缓冲图像
     *
     * @param content 二维码内容
     * @param height  二维码长度
     * @param width   二维码宽度
     * @return {@link BufferedImage}
     */
    public static BufferedImage simpleQrCode(String content, int height, int width) {
        return simpleQrCode(content, height, width, FORMAT, ERR_LEVEL);
    }

    /**
     * 获取简单二维码缓冲图像
     *
     * @param content 二维码内容
     * @param height  二维码长度
     * @param width   二维码宽度
     * @param format  二维码格式化
     * @param level   二维码纠错等级
     * @return {@link BufferedImage}
     */
    public static BufferedImage simpleQrCode(String content, int height, int width,
                                             String format, ErrorCorrectionLevel level) {
        // 内容检查
        checkParam(content);

        // 检查二维码长宽
        checkHeightAndWidth(height, width);

        // 参数格式化
        format = StringUtils.isEmpty(format) ? "UTF-8" : format;
        level = Objects.isNull(level) ? ErrorCorrectionLevel.M : level;

        // 编码提示类型配置
        Map<EncodeHintType, Object> map = new HashMap<>();
        map.put(EncodeHintType.CHARACTER_SET, format);
        map.put(EncodeHintType.ERROR_CORRECTION, level);
        map.put(EncodeHintType.MARGIN, MARGIN);

        // 获取二维码位图矩阵
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, map);
        } catch (WriterException e) {
            log.error("获取二维码位图矩阵失败=【{}】", e.getMessage());
            throw new RuntimeException("获取二维码位图矩阵失败");
        }

        // 获取二维码图像配置
        MatrixToImageConfig matrixToImageConfig = new MatrixToImageConfig(0xFF000001, 0xFFFFFFFF);

        // 获取二维码缓冲图像
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix, matrixToImageConfig);

        return bufferedImage;
    }

    // -----------------------------------------------------------------------------------------------
    // Logo QR CODE
    // -----------------------------------------------------------------------------------------------

    /**
     * 填充 logo 到二维码图片中
     *
     * @param qrCodeMatrixImage 原二维码图片矩阵
     * @param logoFile          logo图片
     * @return
     * @throws IOException
     */
    public static BufferedImage logoQrCode(BufferedImage qrCodeMatrixImage, File logoFile) throws IOException {
        BufferedImage logoMatrixImage = ImageIO.read(logoFile);
        BufferedImage bufferedImage = logoQrCode(qrCodeMatrixImage, logoMatrixImage);
        return bufferedImage;
    }

    /**
     * 填充 logo 到二维码图片中
     *
     * @param qrCodeMatrixImage 原二维码图片矩阵
     * @param inputStream       logo输入流
     * @return
     * @throws IOException
     */
    public static BufferedImage logoQrCode(BufferedImage qrCodeMatrixImage, ByteArrayOutputStream byteArrayOutputStream) throws IOException {
        InputStream inputStream = null;
        BufferedImage logoMatrixImage = null;
        try {
            inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            logoMatrixImage = ImageIO.read(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        BufferedImage bufferedImage = logoQrCode(qrCodeMatrixImage, logoMatrixImage);
        return bufferedImage;
    }

    /**
     * 填充 logo 到二维码图片中：只能读取一次
     *
     * @param qrCodeMatrixImage 原二维码图片矩阵
     * @param inputStream       logo输入流
     * @return
     * @throws IOException
     */
    public static BufferedImage logoQrCode(BufferedImage qrCodeMatrixImage, InputStream inputStream) throws IOException {
        BufferedImage logoMatrixImage = null;
        try {
            logoMatrixImage = ImageIO.read(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        BufferedImage bufferedImage = logoQrCode(qrCodeMatrixImage, logoMatrixImage);
        return bufferedImage;
    }

    /**
     * 填充 logo 到二维码图片中
     *
     * @param qrCodeMatrixImage 原二维码图片矩阵
     * @param imageInputStream  logo图片输入流
     * @return
     * @throws IOException
     */
    public static BufferedImage logoQrCode(BufferedImage qrCodeMatrixImage, ImageInputStream imageInputStream) throws IOException {
        BufferedImage logoMatrixImage = null;
        try {
            logoMatrixImage = ImageIO.read(imageInputStream);
        } finally {
            if (imageInputStream != null) {
                imageInputStream.close();
            }
        }
        BufferedImage bufferedImage = logoQrCode(qrCodeMatrixImage, logoMatrixImage);
        return bufferedImage;
    }

    /**
     * 填充 logo 到二维码图片中
     *
     * @param qrCodeMatrixImage 原二维码图片矩阵
     * @param url               logo URL
     * @return
     * @throws IOException
     */
    public static BufferedImage logoQrCode(BufferedImage qrCodeMatrixImage, URL url) throws IOException {
        BufferedImage logoMatrixImage = ImageIO.read(url);
        BufferedImage bufferedImage = logoQrCode(qrCodeMatrixImage, logoMatrixImage);
        return bufferedImage;
    }

    /**
     * 填充 logo 到二维码图片中
     *
     * @param qrCodeMatrixImage 原二维码图片矩阵
     * @param logoMatrixImage   logo二维码图片矩阵
     * @return
     * @throws IOException
     */
    public static BufferedImage logoQrCode(BufferedImage qrCodeMatrixImage, BufferedImage logoMatrixImage) {
        int height = qrCodeMatrixImage.getHeight();
        int width = qrCodeMatrixImage.getWidth();

        Graphics2D imageGraphics = null;
        try {
            // 读取二维码、构建图像
            imageGraphics = qrCodeMatrixImage.createGraphics();

            // logo 绘制
            imageGraphics.drawImage(logoMatrixImage, width / 5 * 2, height / 5 * 2, width / 5, height / 5, null);
            BasicStroke basicStroke = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            imageGraphics.setStroke(basicStroke);

            RoundRectangle2D.Float round = new RoundRectangle2D.Float(width / 5 * 2, height / 5 * 2, width / 5, height / 5, 20, 20);
            imageGraphics.setColor(Color.white);
            // 绘制圆弧矩形
            imageGraphics.draw(round);

            // 设置logo 有一道灰色边框
            BasicStroke stroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            // 设置笔画对象
            imageGraphics.setStroke(stroke);
            // 指定弧度的圆角矩形
            RoundRectangle2D.Float round2 = new RoundRectangle2D.Float(width / 5 * 2 + 2, height / 5 * 2 + 2, width / 5 - 4, height / 5 - 4, 20, 20);
            imageGraphics.setColor(new Color(128, 128, 128));

            // 绘制圆弧矩形
            imageGraphics.draw(round2);
        } finally {
            // 释放资源Ω
            if (!Objects.isNull(imageGraphics)) {
                imageGraphics.dispose();
            }
            qrCodeMatrixImage.flush();
        }

        return qrCodeMatrixImage;
    }

    // -----------------------------------------------------------------------------------------------
    // 生成带文字的二维码
    // -----------------------------------------------------------------------------------------------

    /**
     * 生成带文字的二维码
     * <p>
     * 注意：在生成中间的文字时需要注意其覆盖区域不能太大，需要调整文字长度、字体大小防止扫码无法识别！
     *
     * @param bufferedImage 二维码缓冲图像
     * @param font          字体
     * @param color         字体颜色
     * @param topText       顶部文字
     * @param centerText    中部文字
     * @param bottomText    底部文字
     * @return
     */
    public static BufferedImage textQrCode(BufferedImage bufferedImage, Font font, Color color,
                                           String topText, String centerText, String bottomText) {
        // check text
        font = checkText(topText, centerText, bottomText, font);

        // 获取二维码的宽高
        int imageWidth = bufferedImage.getWidth();
        int imageHeight = bufferedImage.getHeight();

        Graphics graphics = null;
        try {
            graphics = bufferedImage.createGraphics();
            graphics.setFont(font);
            graphics.setColor(color);

            // 获取顶部字体的宽、高
            drawText(bufferedImage, font, topText, imageWidth, imageHeight, graphics, TextPosEnum.TOP);
            drawText(bufferedImage, font, centerText, imageWidth, imageHeight, graphics, TextPosEnum.CENTER);
            drawText(bufferedImage, font, bottomText, imageWidth, imageHeight, graphics, TextPosEnum.BOTTOM);
        } finally {
            if (graphics != null) {
                graphics.dispose();
            }
        }

        return bufferedImage;
    }

    // -----------------------------------------------------------------------------------------------
    // 批量生成二维码
    // -----------------------------------------------------------------------------------------------

    /**
     * 批量生成二维码：纯文字填充
     *
     * @param arrays 二维码列表对象
     * @return
     * @throws Exception
     */
    public static List<BufferedImage> batchTextQrCode(List<QrCodeContent> arrays) throws Exception {
        int size = arrays.size();
        List<BufferedImage> list = new ArrayList<>(size);
        Font font = new Font("宋体", Font.ITALIC, 24);

        CountDownLatch countDownLatch = new CountDownLatch(size);

        long start = Instant.now().toEpochMilli();
        arrays.parallelStream().forEach(m -> {
            threadPoolExecutor.execute(() -> {
                // gen qr code
                BufferedImage bufferedImage = simpleQrCode(m.getContent());

                // fill text
                bufferedImage = textQrCode(bufferedImage, font, Color.BLACK, m.getTopText(), m.getCenterText(), m.getBottomText());

                // add List
                list.add(bufferedImage);

                countDownLatch.countDown();
            });
        });

        countDownLatch.await();
        long end = Instant.now().toEpochMilli();

        log.info("生成二维码共计耗时 {} ms", end - start);

        return list;
    }


    /**
     * 批量生成二维码
     *
     * @param arrays                二维码列表对象
     * @param byteArrayOutputStream 字节数组流、这里不能使用 InputStream，因为只能被读取一次
     * @return
     * @throws Exception
     */
    public static List<BufferedImage> batchLogoQrCode(List<QrCodeContent> arrays, ByteArrayOutputStream byteArrayOutputStream) throws Exception {
        // check byteArrayOutputStream
        if (byteArrayOutputStream == null) {
            throw new FileNotFoundException("ByteArrayOutputStream can not be null");
        }

        int size = arrays.size();
        List<BufferedImage> list = new ArrayList<>(size);
        Font font = new Font("宋体", Font.ITALIC, 24);

        CountDownLatch countDownLatch = new CountDownLatch(size);

        long start = Instant.now().toEpochMilli();
        arrays.parallelStream().forEach(m -> {
            threadPoolExecutor.execute(() -> {
                // gen qr code
                BufferedImage bufferedImage = simpleQrCode(m.getContent());

                // file picture
                try {
                    bufferedImage = logoQrCode(bufferedImage, byteArrayOutputStream);
                    // 默认处理：如果存在 logo 填充则不需要再增加中心文字
                    m.setCenterText("");
                } catch (IOException e) {
                    log.error("填充内容为 =【{}】 的 logo 发生错误=【{}】，提前终止！", m.getContent(), e.getMessage());
                    return;
                }

                // fill text
                bufferedImage = textQrCode(bufferedImage, font, Color.BLACK, m.getTopText(), m.getCenterText(), m.getBottomText());

                // add List
                list.add(bufferedImage);

                countDownLatch.countDown();
            });
        });

        countDownLatch.await();
        long end = Instant.now().toEpochMilli();

        log.info("生成二维码共计耗时 {} ms", end - start);

        return list;
    }


    /**
     * 批量生成二维码
     *
     * @param arrays   二维码列表对象
     * @param logoFile 二维码文件
     * @return
     * @throws Exception
     */
    public static List<BufferedImage> batchLogoQrCode(List<QrCodeContent> arrays, File logoFile) throws Exception {
        // check file
        if (ObjectUtils.isEmpty(logoFile) || !logoFile.exists()) {
            throw new FileNotFoundException("Not fount this File");
        }

        int size = arrays.size();
        List<BufferedImage> list = new ArrayList<>(size);
        Font font = new Font("宋体", Font.ITALIC, 24);

        CountDownLatch countDownLatch = new CountDownLatch(size);

        long start = Instant.now().toEpochMilli();
        arrays.parallelStream().forEach(m -> {
            threadPoolExecutor.execute(() -> {
                // gen qr code
                BufferedImage bufferedImage = simpleQrCode(m.getContent());

                // file picture
                try {
                    bufferedImage = logoQrCode(bufferedImage, logoFile);
                    // 默认处理：如果存在 logo 填充则不需要再增加中心文字
                    m.setCenterText("");
                } catch (IOException e) {
                    log.error("填充内容为 =【{}】 的 logo 发生错误=【{}】，提前终止！", m.getContent(), e.getMessage());
                    return;
                }

                // fill text
                bufferedImage = textQrCode(bufferedImage, font, Color.BLACK, m.getTopText(), m.getCenterText(), m.getBottomText());

                // add List
                list.add(bufferedImage);

                countDownLatch.countDown();
            });
        });

        countDownLatch.await();
        long end = Instant.now().toEpochMilli();

        log.info("生成二维码共计耗时 {} ms", end - start);

        return list;
    }

    /**
     * 批量生成二维码
     *
     * @param arrays  二维码列表对象
     * @param logoURL 二维码URL
     * @return
     * @throws Exception
     */
    public static List<BufferedImage> batchLogoQrCode(List<QrCodeContent> arrays, URL url) throws Exception {
        // check URL
        try {
            url.openStream();
        } catch (MalformedURLException e) {
            throw new MalformedURLException("URL 格式错误");
        } catch (IOException e) {
            throw new IOException("无法读取 URL 链接");
        }

        int size = arrays.size();
        List<BufferedImage> list = new ArrayList<>(size);
        Font font = new Font("宋体", Font.ITALIC, 24);

        CountDownLatch countDownLatch = new CountDownLatch(size);

        long start = Instant.now().toEpochMilli();

        final URL finalUrl = url;
        arrays.parallelStream().forEach(m -> {
            threadPoolExecutor.execute(() -> {
                // gen qr code
                BufferedImage bufferedImage = simpleQrCode(m.getContent());

                // file text and picture
                try {
                    bufferedImage = logoQrCode(bufferedImage, finalUrl);
                    // 默认处理：如果存在 logo 填充则不需要再增加中心文字
                    m.setCenterText("");
                } catch (IOException e) {
                    log.error("填充内容为 =【{}】 的 logo 发生错误=【{}】，提前终止！", m.getContent(), e.getMessage());
                    return;
                }

                // fill text
                bufferedImage = textQrCode(bufferedImage, font, Color.BLACK, m.getTopText(), m.getCenterText(), m.getBottomText());

                // add List
                list.add(bufferedImage);

                countDownLatch.countDown();
            });
        });

        countDownLatch.await();
        long end = Instant.now().toEpochMilli();

        log.info("生成二维码共计耗时 {} ms", end - start);

        return list;
    }

    // -----------------------------------------------------------------------------------------------
    // WRITE BufferedImage TO File/OutputStream/ImageOutputStream
    // -----------------------------------------------------------------------------------------------

    /**
     * 图片缓冲对象写入到文件
     *
     * @param img      图片缓冲对象
     * @param fileType 格式化文件名称，如：png、jpg
     * @param filepath 文件路径
     * @throws IOException
     */
    public static void write2File(BufferedImage img, String fileType, String filepath) throws IOException {
        try {
            ImageIO.write(img, fileType, new File(filepath));
        } finally {
            // 释放资源
            if (img != null) {
                img.flush();
            }
        }
    }

    /**
     * 图片缓冲对象写入到输出流
     *
     * @param img          图片流
     * @param fileType     格式化文件名称，如：png、jpg
     * @param outputStream 输出流
     * @throws IOException
     */
    public static void write2Stream(BufferedImage img, String fileType, OutputStream outputStream) throws IOException {
        try {
            ImageIO.write(img, fileType, outputStream);
        } finally {
            // 释放资源
            if (img != null) {
                img.flush();
            }
        }
    }

    /**
     * 图片缓冲对象写入到图片输出流
     *
     * @param img               图片流
     * @param fileType          格式化文件名称，如：png、jpg
     * @param imageOutputStream 图片输出流
     * @throws IOException
     */
    public static void write2Stream(BufferedImage img, String fileType, ImageOutputStream imageOutputStream) throws IOException {
        try {
            ImageIO.write(img, fileType, imageOutputStream);
        } finally {
            // 释放资源
            if (img != null) {
                img.flush();
            }
        }
    }

    // -----------------------------------------------------------------------------------------------
    // IDENTIFY QR CODE
    // -----------------------------------------------------------------------------------------------

    /**
     * 识别二维码内容
     *
     * @param url URL链接
     * @return
     * @throws IOException
     * @throws NotFoundException
     */
    public static Result identifyQrCode(URL url) throws IOException, NotFoundException {
        BufferedImage bufferedImage = ImageIO.read(url);
        Result result = identifyQrCode(bufferedImage);
        return result;
    }

    /**
     * 识别二维码内容
     *
     * @param file 文件
     * @return
     * @throws IOException
     * @throws NotFoundException
     */
    public static Result identifyQrCode(File file) throws IOException, NotFoundException {
        BufferedImage bufferedImage = ImageIO.read(file);
        Result result = identifyQrCode(bufferedImage);
        return result;
    }

    /**
     * 识别二维码内容
     *
     * @param inputStream 输入流
     * @return
     * @throws IOException
     * @throws NotFoundException
     */
    public static Result identifyQrCode(InputStream inputStream) throws IOException, NotFoundException {
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        Result result = identifyQrCode(bufferedImage);
        return result;
    }

    /**
     * 识别二维码内容
     *
     * @param imageInputStream 图片输入流
     * @return
     * @throws IOException
     * @throws NotFoundException
     */
    public static Result identifyQrCode(ImageInputStream imageInputStream) throws IOException, NotFoundException {
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(imageInputStream);
        } finally {
            if (imageInputStream != null) {
                imageInputStream.close();
            }
        }
        Result result = identifyQrCode(bufferedImage);
        return result;
    }

    /**
     * 识别二维码内容
     *
     * @param bufferedImage 缓冲图像
     * @return
     * @throws IOException
     * @throws NotFoundException
     */
    public static Result identifyQrCode(BufferedImage bufferedImage) throws IOException, NotFoundException {
        Result result = null;
        try {
            // 读取指定的二维码文件
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage)));

            // 定义二维码参数
            Map hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            result = new MultiFormatReader().decode(binaryBitmap, hints);

            // 输出相关的二维码信息
            log.info("解析二维码，二维码格式类型=【{}】、二维码内容=【{}】", result.getBarcodeFormat(), result.getText());
        } finally {
            // 释放资源
            bufferedImage.flush();
        }

        return result;
    }

    // -----------------------------------------------------------------------------------------------
    // Utilities
    // -----------------------------------------------------------------------------------------------

    /**
     * 内容检查
     *
     * @param obj
     */
    private static void checkParam(Object obj) throws RuntimeException {
        if ((obj instanceof String) && (StringUtils.isEmpty(obj))) {
            throw new RuntimeException("param can not be null or empty");
        }
        if ((obj instanceof BufferedImage) && (obj == null)) {
            throw new RuntimeException("param can not be is null");
        }
    }

    /**
     * 二维码长宽检查
     *
     * @param height
     * @param width
     * @throws RuntimeException
     */
    private static void checkHeightAndWidth(int height, int width) throws RuntimeException {
        if (height < 0 || width < 0) {
            throw new RuntimeException("param must > 0");
        }
    }

    /**
     * 根据不同位置绘制图像
     *
     * @param bufferedImage
     * @param font
     * @param text
     * @param imageWidth
     * @param imageHeight
     * @param graphics
     * @param top
     */
    private static void drawText(BufferedImage bufferedImage, Font font, String text, int imageWidth, int imageHeight, Graphics graphics, TextPosEnum posEnum) {
        if (!StringUtils.isEmpty(text)) {
            text = new String(text.trim().getBytes(), Charset.forName("UTF-8"));
            FontMetrics metrics = FontDesignMetrics.getMetrics(font);
            int fontWidth = metrics.stringWidth(text);
            int fontHeight = metrics.getHeight();

            // 绘制图像
            fillText(bufferedImage, text, fontWidth, fontHeight, imageWidth, imageHeight, graphics, posEnum);
        }
    }

    /**
     * 填充文字
     *
     * @param bufferedImage 图片缓冲流
     * @param text          填充文字内容
     * @param fontWidth     文字宽度
     * @param fontHeight    文字长度
     * @param imageWidth    图片宽度
     * @param imageHeight   图片长度
     * @param graphics      绘图对象
     * @param textPosEnum   位置
     */
    private static void fillText(BufferedImage bufferedImage, String text,
                                 int fontWidth, int fontHeight,
                                 int imageWidth, int imageHeight,
                                 Graphics graphics, TextPosEnum posEnum) {
        int startX;
        int startY;
        Integer posEnumCode = posEnum.getCode();
        if (TextPosEnum.TOP.getCode().equals(posEnumCode)) {
            startX = (imageWidth - fontWidth) / 2 < 0 ? 0 : (imageWidth - fontWidth) / 2;
            startY = fontHeight * 3 / 2;
            graphics.drawString(text, startX, startY);
        }
        if (TextPosEnum.CENTER.getCode().equals(posEnumCode)) {
            startX = (imageWidth - fontWidth) / 2 + 5;
            startY = imageHeight / 2 + fontHeight / 2 - (fontHeight / 4) + 5;
            int endX = startX + fontWidth + 5;
            int endY = startY + 5;
            // 填充文字区域背景
            for (int x = 0; x < imageWidth; x++) {
                for (int y = 0; y < imageHeight; y++) {
                    // 中心文字填充区域背景设置为空白
                    if (x > (startX - 10) && x < endX && y > (startY - fontHeight) && y < endY) {
                        bufferedImage.setRGB(x, y, BACKGROUND_COLOR);
                    }
                }
            }
            graphics.drawString(text, startX, startY);
        }
        if (TextPosEnum.BOTTOM.getCode().equals(posEnumCode)) {
            startX = (imageWidth - fontWidth) / 2 < 0 ? 0 : (imageWidth - fontWidth) / 2;
            startY = imageHeight - fontHeight;
            graphics.drawString(text, startX, startY);
        }
    }

    /**
     * InputStream 转 ByteArrayOutputStream
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    private static ByteArrayOutputStream inputStream2ByteStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) > -1) {
            baos.write(buffer, 0, len);
        }
        return baos;
    }

    /**
     * 检查文字二维码
     *
     * @param topText
     * @param centerText
     * @param bottomText
     * @param font
     */
    private static Font checkText(String topText, String centerText, String bottomText, Font font) {
        int fontSize = font.getSize();

        // 不做处理
        if (topText.length() < 8 && centerText.length() < 8 && bottomText.length() < 8 && fontSize < 24) {
            return font;
        }

        // 如果字体过大
        if (fontSize > 20) {
            font = new Font(font.getName(), font.getStyle(), 20);
        }

        // 如果文本过长
        if (topText.length() > 15 || centerText.length() > 15 || bottomText.length() > 15) {
            font = new Font(font.getName(), font.getStyle(), 16);
        }

        return font;
    }

    // -----------------------------------------------------------------------------------------------
    // TEST
    // -----------------------------------------------------------------------------------------------

    @SneakyThrows
    public static void main(String[] args) {
        //BufferedImage bufferedImage = simpleQrCode("去他妈的加班！！！");
        //write2File(bufferedImage, "png", "/Users/rexlin600/Desktop/1.png");
        //// text
        //BufferedImage bufferedImage2 = textQrCode(bufferedImage, new Font("宋体", Font.ITALIC, 24), "天王盖地虎", "", "加班不辛苦");
        //// logo
        //BufferedImage bufferedImage3 = logoQrCode(bufferedImage2, new File("/Users/rexlin600/Pictures/微信公众号-图片/SpringFramework.png"));
        //write2File(bufferedImage3, "png", "/Users/rexlin600/Desktop/3.png");

        List<QrCodeContent> list = new ArrayList<>();
        list.add(new QrCodeContent("FUCK-1", "fuck-1", "", ""));
        list.add(new QrCodeContent("FUCK-2", "", "fuck-2", ""));
        list.add(new QrCodeContent("FUCK-3", "", "", "fuck-3"));
        list.add(new QrCodeContent("FUCK-4", "fuck-4", "fuck-4", "fuck-4"));

        // batch gen qr code
        //File file = new File("C:\\Users\\hekunlin\\Pictures\\golang.jpg");
        //List<BufferedImage> imageList = batchLogoQrCode(list, file);

        //URL url = new URL("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1583127516250&di=3cf42d0daf3c7df9154f277119e6cc8c&imgtype=0&src=http%3A%2F%2Fa0.att.hudong.com%2F78%2F52%2F01200000123847134434529793168.jpg");
        //List<BufferedImage> imageList = batchLogoQrCode(list, url);

        List<BufferedImage> imageList = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        InputStream inputStream = null;
        try {
            File file = new File("C:\\Users\\hekunlin\\Pictures\\golang.jpg");
            inputStream = new FileInputStream(file);
            byteArrayOutputStream = inputStream2ByteStream(inputStream);
            imageList = batchLogoQrCode(list, byteArrayOutputStream);
            //imageList = batchTextQrCode(list);
        } finally {
            // 释放资源
            log.info("close stream");
            byteArrayOutputStream.flush();
            inputStream.close();
        }

        // batch save
        for (int i = 0; i < imageList.size(); i++) {
            log.info("generate no.{} qrcode", (i + 1));
            write2File(imageList.get(i), "png", "C:\\Users\\hekunlin\\Pictures\\QRCode\\" + (i + 1) + ".png");
        }
    }


}