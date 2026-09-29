package util;

import java.util.Random;

/**
 * ============================================================
 * 【随机码工具类 CodeUtil】—— 验证码 / 用户ID / 随机数
 * ============================================================
 *
 * 【为什么要有这个类】
 * 优化前，"生成验证码"的代码在 Login 和 Test 里各写了一份，
 * "生成用户ID"的代码写在 User 里，三处都是同一个模式：
 *     建池子 → 随机取字符 → 拼起来
 * 这是典型的"重复的知识"（DRY 原则）：改一处需求要动三个文件。
 *
 * 【优化清单】
 *   ① Login.getCode() 和 Test.getCode() 合并到这里，只留一份
 *   ② User.createId() 移到这里——"造随机码"是工具活儿，不该压在 User 身上
 *   ③ pool1 / pool2 这种没信息量的名字 → LETTERS / DIGITS
 *   ④ 池子和 Random 从"方法内局部变量"提为"类常量"：
 *      【原代码】每次调用 getCode() 都会执行 new Random()，实打实创建新对象
 *      【优化后】整个程序运行期间只有一个 Random，反复使用
 *   ⑤ 6、5 这类数字抽成常量，改需求时只动一处
 *
 * 【注意】工具类的方法都是 static——因为它们不依赖任何对象的状态，
 *        纯粹是"输入→输出"的计算，没必要先 new 一个对象再调用。
 */
public class CodeUtil {

    // ==================== 常量区 ====================

    /**
     * 【优化】原代码写在各方法内部，每次调用都重新创建：
     *     String pool1="ABCDEF...";   String pool2="0123456789";   Random r=new Random();
     * 提到类级别后，整个程序只创建一次。
     *
     * 命名规范：static final 的常量名用"全大写+下划线"，
     * 一眼就能看出"这是配置，不是会变的变量"。
     */
    private static final Random RANDOM = new Random();

    /** 字母池：验证码里的字母从这里随机取 */
    private static final String LETTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /** 数字池：验证码里的数字、ID 里的数字都从这里取 */
    private static final String DIGITS = "0123456789";

    /** 验证码位数 */
    private static final int CODE_LENGTH = 6;

    /** 用户ID前缀 */
    private static final String ID_PREFIX = "czjt";

    /** 用户ID里随机数字的个数 */
    private static final int ID_DIGIT_COUNT = 5;

    /**
     * 【优化】私有构造方法——工具类不允许被 new。
     * 因为所有方法都是 static，new 出对象也没用。
     * 把它设成 private，别人写 new CodeUtil() 会直接编译报错，
     * 等于用编译器帮你拦住误用。
     */
    private CodeUtil() {
    }

    // ==================== 生成验证码 ====================

    /**
     * 生成 6 位验证码，保证"至少有 1 个字母 + 至少有 1 个数字"。
     *
     * 【思路】先随机生成 6 个字母，再把"随机一个位置"替换成数字。
     * 这样一次就成功，不需要"生成完再检查有没有数字、没有就重来"的试错。
     *
     * 【原代码】
     *     public static String getCode(){
     *         String pool1="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
     *         String pool2="0123456789";
     *         Random r=new Random();
     *         StringBuilder sb=new StringBuilder();
     *         for (int i = 0; i < 6; i++) {
     *             int index=r.nextInt(pool1.length());
     *             sb.append(pool1.charAt(index));
     *         }
     *         sb.setCharAt(r.nextInt(sb.length()),pool2.charAt(r.nextInt(pool2.length())));
     *         return sb.toString();
     *     }
     *
     * 【优化点明细】
     *   ① 池子和 Random 提到类外部，不再每次调用都创建
     *   ② pool1/pool2 → LETTERS/DIGITS（名字要一眼看出内容）
     *   ③ 6 → CODE_LENGTH（消除魔法数字）
     *   ④ 最后那行太长、两个 nextInt 挤在一行 → 拆成三步，每步自己说话
     *
     * @return 6位验证码字符串
     */
    public static String getCode() {
        StringBuilder sb = new StringBuilder();

        // 第1步：先填 CODE_LENGTH 个字母
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(LETTERS.length());
            sb.append(LETTERS.charAt(index));
        }

        // 【优化】原代码这一行塞了两次 nextInt、一次 charAt、一次 setCharAt，
        // 眼睛要来回跳。拆开之后每一步都有名字，出错也知道是哪一步。
        // 【原代码】sb.setCharAt(r.nextInt(sb.length()), pool2.charAt(r.nextInt(pool2.length())));
        int position = RANDOM.nextInt(sb.length());              // 挑哪个位置替换
        char digit = DIGITS.charAt(RANDOM.nextInt(DIGITS.length()));  // 换成哪个数字
        sb.setCharAt(position, digit);

        return sb.toString();
    }

    // ==================== 生成用户ID ====================

    /**
     * 生成用户ID，格式："czjt" + 5位随机数字。
     *
     * 【原代码】这个方法原本写在 User 类里，叫 createID()，而且是 public：
     *     public String createID(){
     *         StringBuilder sb=new StringBuilder("czjt");
     *         Random r=new Random();
     *         for (int i = 0; i < 5; i++) {
     *             int num = r.nextInt(10);
     *             sb.append(num);
     *         }
     *         return sb.toString();
     *     }
     *
     * 【优化点明细】
     *   ① 从 User 挪到 CodeUtil——User 的职责是"表示一个用户"，
     *      不是"生成随机码"。职责要放对地方（单一职责）。
     *   ② 从"实例方法"变"静态方法"——它不需要读取任何 User 的数据，
     *      纯粹是"调一下生成一个码"，所以 static 更合适。
     *   ③ 前缀 "czjt" 和长度 5 抽成常量。
     */
    public static String createId() {
        StringBuilder sb = new StringBuilder(ID_PREFIX);
        for (int i = 0; i < ID_DIGIT_COUNT; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    // ==================== 随机数 ====================

    /**
     * 生成 [min, max] 之间的随机整数（含两端）。
     *
     * 【为什么需要这个方法】
     * 原代码里用到了 r.nextInt(20, 41) 这种"两个参数的 nextInt"，
     * 但那是 **Java 17 才新增**的写法，JDK 8 里根本没有这个方法！
     *
     * ⚠️ 蓝桥杯竞赛环境是 Eclipse + JDK 1.8，
     *    如果比赛时写 r.nextInt(20, 41)，会直接编译报错。
     *    所以统一用这个方法封装，底层是 JDK 1.8 就有的写法。
     *
     * 【原理】nextInt(n) 返回 0 ~ n-1，所以：
     *        想得到 [min, max]，就让范围是 max-min+1 个数，再加回 min。
     *
     * @param min 最小值（含）
     * @param max 最大值（含）
     * @return [min, max] 区间的随机整数
     */
    public static int randomInt(int min, int max) {
        return min + RANDOM.nextInt(max - min + 1);
    }
}
