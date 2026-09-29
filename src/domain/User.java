package domain;

import util.CodeUtil;

/**
 * ============================================================
 * 【用户实体 User】—— 表示"一个注册用户"
 * ============================================================
 *
 * 【它在项目里的位置】
 *   这是 domain 包（数据层）的一员，只负责"装数据"，不管界面怎么显示，
 *   也不管注册流程怎么走。界面的活儿交给 ui 包。
 *   这样分工的好处：将来把命令行界面换成图形界面，User 一行都不用改。
 *
 * 【本次优化清单】
 *   ① 字段本来就是 private（这块你写对了 ✓），保持不动
 *   ② 字段加注释，说明每个字段存在的理由
 *   ③ createID() 搬走 → util/CodeUtil.createId()
 *      理由：User 的职责是"表示用户"，不是"生成随机码"。
 *      而且原来的 createID() 是 public 的，外面根本不需要调它。
 *   ④ 补一个 toString()（可选但推荐，见方法上的说明）
 */
public class User {

    /**
     * 用户ID，格式 czjt+5位随机数字。
     * 由构造方法自动生成——"每个用户都该有ID"是 User 自己的规矩，
     * 放在构造里，任何创建 User 的路径都漏不掉。
     */
    private String id;

    /** 用户名（登录用，唯一） */
    private String name;

    /** 密码 */
    private String password;

    /**
     * 账号状态：true=正常，false=被锁定。
     * 初始为 true，连续输错密码后会变成 false。
     */
    private boolean status;

    /**
     * 无参构造：造一个"空白用户"，ID 和状态自动就绪。
     *
     * 【知识点：new 的时候发生了什么】
     * 写 new User() 这一句，JVM 做两件事：
     *   ① 在堆内存里开出空间
     *   ② 自动调用匹配的构造方法（参数为空 → 匹配无参构造）
     * 所以构造方法**不会被主动调用**，只能被 new 触发。
     * 写 u.User() 会直接编译报错——这是它和普通方法最大的区别。
     */
    public User() {
        id = CodeUtil.createId();
        status = true;
    }

    /**
     * 全参构造：一次把用户名和密码都带上。
     *
     * 【优化点】这个方法原本写出来但从没被调用过——
     * register() 一直用的是"先 new 空对象，再 setName/setPassword"。
     * 现在注册流程改成"数据收集完，最后一步才创建对象"，
     * 正好用上它：
     *     User u = new User(username, password);
     * 比原来两行 set 更简洁，也避免了"中途取消却白造了一个对象"。
     */
    public User(String name, String password) {
        id = CodeUtil.createId();
        this.name = name;
        this.password = password;
        status = true;
    }

    // ==================== getter / setter ====================

    public String getId() {
        return id;
    }

    /** 说明：setId 其实不该对外暴露（ID 应该由系统生成、不允许随便改），
     *  但当前没有用到，先保留不动。 */
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * boolean 类型的 getter 用 is 开头（isStatus 而不是 getStatus），
     * 这是 JavaBean 的命名规范，你写对了 ✓
     */
    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    /**
     * 【说明】toString() 是 Object 自带的方法，默认打印"类名@地址"这种看不懂的东西。
     * 重写之后，System.out.println(user) 就能直接打印出有用的信息。
     * 这段先放着不用，等你学到第14章"重写 Object 的 toString/equals"时再启用。
     *
     * 【原代码】没有这个方法，属于新增（可选优化）
     */
    @Override
    public String toString() {
        return "User{id='" + id + "', name='" + name + "', status=" + status + "}";
    }
}
