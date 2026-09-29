package domain;

/**
 * ============================================================
 * 【角色基类 GameCharacter】—— 所有游戏角色的共同祖先
 * ============================================================
 *
 * 【继承结构】
 *          GameCharacter（本类）  共性：名字/生命/攻击/防御 + 受伤/治疗/死亡
 *            ├── HeroCharacter   个性：技能列表
 *            └── EnemyCharacter  个性：技能 + 防御姿态（重写 takeDamage）
 *
 * 【为什么类名从 character 改成 GameCharacter】
 *   ① Java 命名规范：类名必须"大驼峰"，首字母大写。原代码写的是
 *      public class character —— 小写开头，看起来像个变量名，必须改。
 *   ② 那直接改成 Character 行不行？**不行**。
 *      JDK 自带一个 java.lang.Character 类（就是第14章要学的"包装类"，
 *      把 char 包装成对象用的）。如果你也叫 Character，两个类同名，
 *      写代码时 IDEA 会不知道你指的是哪一个，容易出乱子。
 *      → 起类名要避开 JDK 已有的名字，这是经验。
 *
 * 【本次优化清单】
 *   ① 类名 character → GameCharacter
 *   ② 字段全部 private + 提供 getter/setter
 *      【原代码】public String name; public int HP; public int maxHP; ...
 *      五个字段全是 public，谁都能随手改。比如外面写 player.HP = -999
 *      也没人拦着，游戏直接崩。这就是第11章"权限修饰符"要解决的问题。
 *   ③ 字段名 HP → hp、maxHP → maxHp（变量名用小驼峰，规范）
 *   ④ heal()/takeDamage() 改为调用 setHp()，复用同一套"边界保护"
 *   ⑤ show() 改用 getter 读值
 */
public class GameCharacter {

    // ==================== 字段区（全部 private） ====================

    /** 角色名字 */
    private String name;

    /** 当前生命值 */
    private int hp;

    /** 生命值上限（治疗时不能超过它） */
    private int maxHp;

    /** 攻击力 */
    private int attack;

    /** 防御力 */
    private int defense;

    // ==================== 构造方法 ====================

    /**
     * 无参构造。
     * 【说明】目前代码里没有直接用到它，但保留着——
     * 因为子类 HeroCharacter / EnemyCharacter 的无参构造会通过
     * super() 调用它。如果删掉，子类的无参构造就会编译报错。
     */
    public GameCharacter() {
    }

    /**
     * 全参构造：一次性把角色造好。
     *
     * 【注意】this.maxHp = hp 这行的含义是：
     * "初始生命值同时就是生命上限"——刚出生的角色是满血的。
     * 所以调用方只需要传一次 hp，不需要传两次。
     */
    public GameCharacter(String name, int hp, int attack, int defense) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.attack = attack;
        this.defense = defense;
    }

    // ==================== getter / setter ====================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHp() {
        return hp;
    }

    /**
     * 设置生命值——带"边界保护"。
     *
     * 【这是封装最实际的好处】不管外面传什么进来，
     * 生命值永远落在 [0, maxHp] 这个合法区间里：
     *     传 -999 → 变成 0（死了，但不会出现负血量）
     *     传 9999 → 变成 maxHp（回满，但不会超过上限）
     *
     * 有了这层保护，takeDamage() 和 heal() 就不用各自再写一遍判断了。
     */
    public void setHp(int hp) {
        if (hp < 0) {
            this.hp = 0;
        } else if (hp > maxHp) {
            this.hp = maxHp;
        } else {
            this.hp = hp;
        }
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    // ==================== 业务方法 ====================

    /** 是否还活着（生命值大于0） */
    public boolean isAlive() {
        return hp > 0;
    }

    /**
     * 治疗：恢复 amount 点生命值。
     *
     * 【原代码】
     *     public void heal(int amount){
     *         HP+=amount;
     *         if(HP>maxHP){
     *             HP=maxHP;
     *         }
     *     }
     *
     * 【优化】"加血"其实就是"设置一个新生命值"，
     * 而 setHp() 里已经做了"不能超过上限"的保护，
     * 所以这里只需要传新值进去，判断交给 setHp。
     * 好处：这条规则只写在一处，哪天规则变了（比如上限改成临时加成）也只改一个地方。
     */
    public void heal(int amount) {
        setHp(hp + amount);
    }

    /**
     * 受到伤害：扣掉 damage 点生命值。
     *
     * 【原代码】
     *     public void takeDamage(int damage){
     *         HP-=damage;
     *         if(HP<0){
     *             HP=0;
     *         }
     *     }
     *
     * 【优化】同理，交给 setHp() 保证"血量不会变成负数"。
     *
     * 【注意】这个方法被子类 EnemyCharacter 重写了（多态！）：
     * 敌人处于"防御姿态"时受到的伤害会减半。
     * 调用方只管调 takeDamage()，具体怎么算由对象的实际类型决定。
     */
    public void takeDamage(int damage) {
        setHp(hp - damage);
    }

    /**
     * 打印当前状态。
     *
     * 【原代码】直接读字段：System.out.println(name+"[当前生命："+HP+",...");
     * 【优化】改用 getter 读值——字段私有之后，类内部虽然仍可直接访问，
     * 但统一用 getter 更一致。
     */
    public void show() {
        System.out.println(name + "[当前生命：" + hp + ",攻击" + attack + ",防御" + defense + "]");
    }
}
