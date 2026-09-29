package util;

import java.util.Scanner;

/**
 * ============================================================
 * 【输入工具类 InputUtil】—— 全项目共用一个 Scanner
 * ============================================================
 *
 * 【优化前的问题】
 * 原代码在每个需要输入的地方都写了一句 new Scanner(System.in)，
 * 一共创建了 6 个 Scanner：
 *     Login.start()      ← 甚至写在 while 循环里，每转一圈就 new 一个
 *     Login.register()
 *     Login.login()
 *     FightGame.gameStart()
 *     FightGame.createPlayerCharacter()
 *     FightGame.playerTurn()
 *
 * 【为什么多个 Scanner 会出问题】
 * Scanner 内部有一块"缓冲区"：它从 System.in 读数据时，
 * 不是一次读一个字符，而是"能拿多少拿多少"先囤起来。
 * 于是：
 *     第一个 Scanner 把数据囤进自己的缓冲区后，
 *     第二个 Scanner 去 System.in 里就什么都看不到了。
 * 表现就是：程序莫名跳过某次输入、或者读到了上一次剩下的内容。
 * 这类 bug 极难排查，因为它时好时坏。
 *
 * 【解决办法】
 * 整个程序只建一个 Scanner，所有输入都走它。数据流向就唯一了。
 *
 * 【顺带解决的一个崩溃】
 * 原代码 createPlayerCharacter() 里用 sc.nextInt() 读属性点，
 * 如果用户输入了 "abc"，程序会直接抛 InputMismatchException 崩掉。
 * 下面的 readInt() 会提示重输，而不是让程序挂掉。
 */
public class InputUtil {

    /**
     * 全项目共用的唯一 Scanner。
     * static  → 类级别，全程序一份
     * final   → 建好后不再换新的
     */
    private static final Scanner SC = new Scanner(System.in);

    /** 工具类不允许被 new */
    private InputUtil() {
    }

    /**
     * 读取一个字符串（以空格/回车分隔）。
     * 相当于原来的 sc.next()。
     */
    public static String readString() {
        return SC.next();
    }

    /**
     * 读取一个整数。
     *
     * 【优化点】原代码直接 sc.nextInt()，用户输入非数字时程序崩溃。
     * 这里改成：先问 hasNextInt()"下一个是不是整数"，
     *   是 → 正常读取
     *   不是 → 把那个非法内容吃掉并提示，然后重新等输入
     * 这样用户输错了游戏也不会退出，只是被要求重输。
     *
     * 【知识点】hasNextInt() 是"试探"，它只看不取；
     *          如果返回 false 而我们不调用 next() 把它吃掉，
     *          下一次 hasNextInt() 还会看到同一个内容 → 死循环。
     *          所以必须用 next() 消费掉那一段非法输入。
     */
    public static int readInt() {
        while (true) {
            if (SC.hasNextInt()) {
                return SC.nextInt();
            }
            String bad = SC.next();
            System.out.print("[" + bad + "] 不是整数，请重新输入：");
        }
    }
}
