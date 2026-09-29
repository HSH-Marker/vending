package domain;

import java.util.ArrayList;

/**
 * ============================================================
 * 【玩家角色 HeroCharacter】—— 继承自 GameCharacter
 * ============================================================
 *
 * 【它比父类多了什么】
 * 只有一个：技能列表 skillList。
 * 属性（名字/生命/攻击/防御）和动作（受伤/治疗/死亡）都从父类继承。
 * 这就是继承的意义——**共性放父类，个性放子类**。
 *
 * 【本次优化清单】
 *   ① skillList 字段虽然是 public，但它是集合（学第13章时还没到封装那步），
 *      这里改成 private + 提供 getter。
 *      同时用 final 修饰：**引用不能换**（不许把 skillList 指向另一个集合），
 *      但集合里的内容照样可以增删——这是第10章"final 锁引用不锁内容"的应用。
 *   ② 两个构造方法里原本各写一遍 skillList=new ArrayList<>()，
 *      改成"字段定义处直接初始化"，写一次就够，两个构造都受益。
 */
public class HeroCharacter extends GameCharacter {

    /**
     * 技能列表。
     *
     * 【原代码】public ArrayList<String> skillList;
     *   - public：外部可以随意替换掉整个列表
     *   - 两个构造方法里各写一遍 skillList = new ArrayList<>();（重复）
     *
     * 【优化】改成字段处初始化 + private + final：
     *   final 在这里锁的是"这个称号只能挂在同一个集合对象上"，
     *   但集合这个"容器"里面装什么、装多少，随时可以改。
     *   （第10章：final 锁引用不锁内容）
     */
    private final ArrayList<String> skillList = new ArrayList<>();

    /**
     * 无参构造：必须先调用父类构造（super()），把父类那部分先造好。
     * 【注意】子类对象在内存里其实是"父类部分 + 子类部分"叠在一起，
     * 所以造子类对象时，父类那部分要先生成。
     */
    public HeroCharacter() {
        super();
    }

    /**
     * 全参构造：自己的四个属性其实全交给父类存，
     * 因为名字/生命/攻击/防御都定义在父类里。
     */
    public HeroCharacter(String name, int hp, int attack, int defense) {
        super(name, hp, attack, defense);
    }

    /**
     * 【优化补充】原代码这里还写了一遍 skillList=new ArrayList<>();
     * 现在已经在字段定义处初始化过了，两处都删掉。
     */

    /** 获取技能列表（因为字段改成 private，外部要通过 getter 拿） */
    public ArrayList<String> getSkillList() {
        return skillList;
    }
}
