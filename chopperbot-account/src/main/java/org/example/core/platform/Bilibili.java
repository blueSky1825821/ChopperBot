package org.example.core.platform;

import org.example.core.factory.PlatformOperation;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * @Description B站登录实现类
 * @Author welsir
 * @Date 2023/9/24 21:18
 */
@Component
public class Bilibili implements PlatformOperation {

    private static final String URL = "https://www.bilibili.com/";
    private static final String CHROME_DRIVER_PROPERTY = "webdriver.chrome.driver";

    @Override
    public Set<Cookie> login(String id, String username) {
        WebDriver loginWebDriver = null;
        WebDriver confirmLoginDriver = null;

        try {
            // 动态设置ChromeDriver路径
            setupChromeDriver();

            ChromeOptions options = new ChromeOptions();
            // 设置浏览器选项，提高稳定性
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--remote-allow-origins=*");

            // 第一步：打开登录页面并等待用户手动登录
            loginWebDriver = new ChromeDriver(options);
            loginWebDriver.get(URL);
            loginWebDriver.manage().deleteAllCookies();
            loginWebDriver.manage().window().maximize();

            System.out.println("请在30秒内完成B站登录操作...");
            Thread.sleep(30000L); // 等待用户手动登录

            Set<Cookie> cookies = loginWebDriver.manage().getCookies();
            loginWebDriver.quit();

            if (cookies.isEmpty()) {
                throw new RuntimeException("未获取到登录Cookie，请确认是否已完成登录");
            }

            System.out.println("获取到的Cookie: " + cookies);

            // 第二步：验证Cookie有效性
            confirmLoginDriver = new ChromeDriver(options);
            confirmLoginDriver.get(URL);
            confirmLoginDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            confirmLoginDriver.manage().deleteAllCookies();

            // 添加有效Cookie
            HashSet<Cookie> validCookies = addValidCookies(confirmLoginDriver, cookies);

            if (validCookies.isEmpty()) {
                throw new RuntimeException("没有有效的Cookie可以添加");
            }

            // 刷新页面应用Cookie
            confirmLoginDriver.navigate().refresh();
            Thread.sleep(5000L);

            // 多种方式验证登录状态
            if (isLoginSuccessful(confirmLoginDriver)) {
                System.out.println("B站登录成功!");
                confirmLoginDriver.quit();
                return validCookies;
            } else {
                throw new RuntimeException("登录验证失败!");
            }

        } catch (Exception e) {
            System.err.println("B站登录过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("B站登录失败: " + e.getMessage(), e);
        } finally {
            // 确保资源被正确释放
            if (loginWebDriver != null) {
                try {
                    loginWebDriver.quit();
                } catch (Exception e) {
                    System.err.println("关闭loginWebDriver时出错: " + e.getMessage());
                }
            }
            if (confirmLoginDriver != null) {
                try {
                    confirmLoginDriver.quit();
                } catch (Exception e) {
                    System.err.println("关闭confirmLoginDriver时出错: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 动态设置ChromeDriver路径
     */
    private void setupChromeDriver() {
        // 检查系统属性是否已设置
        if (System.getProperty(CHROME_DRIVER_PROPERTY) == null) {
            // 尝试多个可能的路径
            String[] possiblePaths = {
                    "D:\\Program Files\\chromedriver-win64\\chromedriver.exe",
                    "C:\\Program Files\\chromedriver-win64\\chromedriver.exe",
                    "D:\\chromedriver-win64\\chromedriver.exe",
                    "C:\\chromedriver-win64\\chromedriver.exe",
                    "./chromedriver.exe",
                    "/usr/local/bin/chromedriver",
                    "/usr/bin/chromedriver"
            };

            for (String path : possiblePaths) {
                File driverFile = new File(path);
                if (driverFile.exists()) {
                    System.setProperty(CHROME_DRIVER_PROPERTY, path);
                    System.out.println("找到ChromeDriver: " + path);
                    return;
                }
            }

            throw new RuntimeException("无法找到ChromeDriver，请确保已正确安装并配置路径");
        }
    }

    /**
     * 添加有效的Cookie
     */
    private HashSet<Cookie> addValidCookies(WebDriver driver, Set<Cookie> cookies) {
        HashSet<Cookie> validCookies = new HashSet<>();

        for (Cookie cookie : cookies) {
            // 验证Cookie的有效性
            if (cookie != null &&
                    cookie.getName() != null && !cookie.getName().trim().isEmpty() &&
                    cookie.getValue() != null && !cookie.getValue().trim().isEmpty()) {

                try {
                    driver.manage().addCookie(cookie);
                    validCookies.add(cookie);
                    System.out.println("添加Cookie: " + cookie.getName());
                } catch (Exception e) {
                    System.err.println("添加Cookie失败: " + cookie.getName() + ", 错误: " + e.getMessage());
                }
            }
        }

        return validCookies;
    }

    /**
     * 验证登录是否成功
     */
    private boolean isLoginSuccessful(WebDriver driver) {
        try {
            // 尝试多种方式检测登录状态

            // 方式1: 检查用户头像元素 (更新后的XPath)
            try {
                WebElement avatar = driver.findElement(By.cssSelector(".header-avatar-wrap, .user-avatar, .avatar"));
                if (avatar != null && avatar.isDisplayed()) {
                    System.out.println("通过头像元素检测到已登录");
                    return true;
                }
            } catch (Exception e) {
                System.out.println("头像元素检测失败: " + e.getMessage());
            }

            // 方式2: 检查用户名称元素
            try {
                WebElement usernameElement = driver.findElement(By.cssSelector(".header-uname, .user-name, .username"));
                if (usernameElement != null && usernameElement.isDisplayed() &&
                        usernameElement.getText() != null && !usernameElement.getText().trim().isEmpty()) {
                    System.out.println("通过用户名元素检测到已登录: " + usernameElement.getText());
                    return true;
                }
            } catch (Exception e) {
                System.out.println("用户名元素检测失败: " + e.getMessage());
            }

            // 方式3: 检查页面URL变化
            String currentUrl = driver.getCurrentUrl();
            if (currentUrl != null && !currentUrl.contains("passport.bilibili.com/login")) {
                System.out.println("通过URL检测到可能已登录");
                // 进一步检查页面内容
                String pageSource = driver.getPageSource();
                if (pageSource != null && pageSource.contains("\"isLogin\":true")) {
                    System.out.println("通过页面内容检测到已登录");
                    return true;
                }
            }

            // 方式4: 执行JavaScript检查
            try {
                Object result = ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("return window.__BILI_USER_INFO__ || window.userInfo || null;");
                if (result != null) {
                    System.out.println("通过JavaScript检测到已登录");
                    return true;
                }
            } catch (Exception e) {
                System.out.println("JavaScript检测失败: " + e.getMessage());
            }

            System.out.println("未检测到登录状态");
            return false;

        } catch (Exception e) {
            System.err.println("登录验证过程中发生错误: " + e.getMessage());
            return false;
        }
    }
}
