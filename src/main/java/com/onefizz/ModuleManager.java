package com.onefizz;

import com.onefizz.modules.*;
import java.util.List;

public class ModuleManager {

    public final KillAura       killAura       = new KillAura();
    public final ESP            esp            = new ESP();
    public final AutoClicker    autoClicker    = new AutoClicker();
    public final Criticals      criticals      = new Criticals();
    public final AutoGapple     autoGapple     = new AutoGapple();
    public final AutoTotem      autoTotem      = new AutoTotem();
    public final BowAimbot      bowAimbot      = new BowAimbot();
    public final Velocity       velocity       = new Velocity();
    public final AntiKnockback  antiKnockback  = new AntiKnockback();
    public final Reach          reach          = new Reach();
    public final HitBox         hitBox         = new HitBox();
    public final AntiBot        antiBot        = new AntiBot();
    public final AutoArmor      autoArmor      = new AutoArmor();
    public final TargetHUD      targetHUD      = new TargetHUD();
    public final Backtrack      backtrack      = new Backtrack();
    public final TargetStrafe   targetStrafe   = new TargetStrafe();

    public final Fullbright     fullbright     = new Fullbright();
    public final XRay           xRay           = new XRay();
    public final MobESP         mobESP         = new MobESP();
    public final ChestESP       chestESP       = new ChestESP();
    public final JumpCircles    jumpCircles    = new JumpCircles();
    public final ChinaHat       chinaHat       = new ChinaHat();
    public final ArrayList      arrayList      = new ArrayList();
    public final PlayerHUD      playerHUD      = new PlayerHUD();
    public final Perspective    perspective    = new Perspective();
    public final Watermark      watermark      = new Watermark();
    public final Trajectories   trajectories   = new Trajectories();
    public final NoRender       noRender       = new NoRender();
    public final BlockOutline   blockOutline   = new BlockOutline();

    public final Flight         flight         = new Flight();
    public final ElytraFly      elytraFly      = new ElytraFly();
    public final BoatFly        boatFly        = new BoatFly();
    public final InventoryMove  inventoryMove  = new InventoryMove();
    public final Timer          timer          = new Timer();
    public final Step           step           = new Step();
    public final Jesus          jesus          = new Jesus();
    public final Sprint         sprint         = new Sprint();
    public final NoFall         noFall         = new NoFall();
    public final NoSlowdown     noSlowdown     = new NoSlowdown();
    public final FastPlace      fastPlace      = new FastPlace();
    public final Speed          speed          = new Speed();
    public final Scaffold       scaffold       = new Scaffold();
    public final Strafe         strafe         = new Strafe();
    public final AutoBridge     autoBridge     = new AutoBridge();
    public final ClickWarp      clickWarp      = new ClickWarp();
    public final AirJump        airJump        = new AirJump();
    public final Spider         spider         = new Spider();
    public final Parkour        parkour        = new Parkour();

    public final AutoEat        autoEat        = new AutoEat();
    public final FastBreak      fastBreak      = new FastBreak();
    public final AntiVoid       antiVoid       = new AntiVoid();
    public final NoHunger       noHunger       = new NoHunger();
    public final NoHurt         noHurt         = new NoHurt();
    public final PacketInspector packetInspector = new PacketInspector();
    public final ChestStealer   chestStealer   = new ChestStealer();
    public final AntiAFK        antiAFK        = new AntiAFK();
    public final AutoLeave      autoLeave      = new AutoLeave();
    public final AutoRespawn    autoRespawn    = new AutoRespawn();
    public final AutoTool       autoTool       = new AutoTool();
    public final Blink          blink          = new Blink();
    public final AutoInvite     autoInvite     = new AutoInvite();
    public final Spammer        spammer        = new Spammer();

    public final AutoRegModule  autoReg        = new AutoRegModule();
    public final StaffListModule staffList      = new StaffListModule();

    public final TriggerBot     triggerBot     = new TriggerBot();
    public final AutoWeapon     autoWeapon     = new AutoWeapon();
    public final FakeLag        fakeLag        = new FakeLag();

    public final DonkeyDupe        donkeyDupe        = new DonkeyDupe();
    public final Disabler          disabler          = new Disabler();
    public final AntiCheatDetector acDetector        = new AntiCheatDetector();
    public final PacketLimiter     packetLimiter     = new PacketLimiter();
    public final TimerBypass       timerBypass       = new TimerBypass();
    public final Phase             phase             = new Phase();

    private final List<Module> all = List.of(
        killAura, esp, autoClicker, criticals, autoGapple, autoTotem, bowAimbot,
        velocity, antiKnockback, reach, hitBox, antiBot, autoArmor, targetHUD,
        backtrack, targetStrafe, triggerBot, autoWeapon, fakeLag,
        fullbright, xRay, mobESP, chestESP, jumpCircles, chinaHat, arrayList, playerHUD, perspective, watermark, trajectories, noRender, blockOutline,
        flight, elytraFly, boatFly, inventoryMove, timer, step, jesus,
        sprint, noFall, noSlowdown, fastPlace, speed, scaffold, strafe, autoBridge, clickWarp, airJump, spider, parkour,
        autoEat, fastBreak, antiVoid, noHunger, noHurt, chestStealer, autoTool, blink, autoInvite, packetInspector, antiAFK, autoLeave, autoRespawn, spammer,
        autoReg, staffList,
        disabler, acDetector, packetLimiter, timerBypass, phase, donkeyDupe
    );

    public KillAura      getKillAura()      { return killAura; }
    public ESP           getEsp()           { return esp; }
    public AutoClicker   getAutoClicker()   { return autoClicker; }
    public Criticals     getCriticals()     { return criticals; }
    public AutoGapple    getAutoGapple()    { return autoGapple; }
    public AutoTotem     getAutoTotem()     { return autoTotem; }
    public BowAimbot     getBowAimbot()     { return bowAimbot; }
    public Velocity      getVelocity()      { return velocity; }
    public AntiKnockback getAntiKnockback() { return antiKnockback; }
    public Reach         getReach()         { return reach; }
    public HitBox        getHitBox()        { return hitBox; }
    public AntiBot       getAntiBot()       { return antiBot; }
    public AutoArmor     getAutoArmor()     { return autoArmor; }
    public TargetHUD     getTargetHUD()     { return targetHUD; }
    public Backtrack     getBacktrack()     { return backtrack; }
    public TargetStrafe  getTargetStrafe()  { return targetStrafe; }

    public Fullbright    getFullbright()    { return fullbright; }
    public XRay          getXRay()          { return xRay; }
    public MobESP        getMobESP()        { return mobESP; }
    public ChestESP      getChestESP()      { return chestESP; }
    public JumpCircles   getJumpCircles()   { return jumpCircles; }
    public ChinaHat      getChinaHat()      { return chinaHat; }
    public ArrayList     getArrayList()     { return arrayList; }
    public PlayerHUD     getPlayerHUD()     { return playerHUD; }
    public Perspective   getPerspective()   { return perspective; }
    public Watermark     getWatermark()     { return watermark; }
    public Trajectories  getTrajectories()  { return trajectories; }
    public NoRender      getNoRender()      { return noRender; }
    public BlockOutline  getBlockOutline()  { return blockOutline; }

    public Flight        getFlight()        { return flight; }
    public ElytraFly     getElytraFly()     { return elytraFly; }
    public BoatFly       getBoatFly()       { return boatFly; }
    public InventoryMove getInventoryMove() { return inventoryMove; }
    public Timer         getTimer()         { return timer; }
    public Step          getStep()          { return step; }
    public Jesus         getJesus()         { return jesus; }
    public Sprint        getSprint()        { return sprint; }
    public NoFall        getNoFall()        { return noFall; }
    public NoSlowdown    getNoSlowdown()    { return noSlowdown; }
    public FastPlace     getFastPlace()     { return fastPlace; }
    public Speed         getSpeed()         { return speed; }
    public Scaffold      getScaffold()      { return scaffold; }
    public Strafe        getStrafe()        { return strafe; }
    public AutoBridge    getAutoBridge()    { return autoBridge; }
    public ClickWarp     getClickWarp()     { return clickWarp; }
    public AirJump       getAirJump()       { return airJump; }
    public Spider        getSpider()        { return spider; }
    public Parkour       getParkour()       { return parkour; }

    public AutoEat       getAutoEat()       { return autoEat; }
    public FastBreak     getFastBreak()     { return fastBreak; }
    public AntiVoid      getAntiVoid()      { return antiVoid; }
    public NoHunger      getNoHunger()      { return noHunger; }
    public NoHurt        getNoHurt()        { return noHurt; }
    public PacketInspector getPacketInspector() { return packetInspector; }
    public ChestStealer  getChestStealer()  { return chestStealer; }
    public AntiAFK       getAntiAFK()       { return antiAFK; }
    public AutoLeave     getAutoLeave()     { return autoLeave; }
    public AutoRespawn   getAutoRespawn()   { return autoRespawn; }
    public AutoTool      getAutoTool()      { return autoTool; }
    public Blink         getBlink()         { return blink; }
    public AutoInvite    getAutoInvite()    { return autoInvite; }
    public Spammer       getSpammer()       { return spammer; }
    public AutoRegModule getAutoReg()       { return autoReg; }
    public StaffListModule getStaffList()   { return staffList; }
    public TriggerBot    getTriggerBot()    { return triggerBot; }
    public AutoWeapon    getAutoWeapon()    { return autoWeapon; }
    public FakeLag       getFakeLag()       { return fakeLag; }

    public DonkeyDupe        getDonkeyDupe()        { return donkeyDupe; }
    public Disabler          getDisabler()          { return disabler; }
    public AntiCheatDetector getAcDetector()        { return acDetector; }
    public PacketLimiter     getPacketLimiter()     { return packetLimiter; }
    public TimerBypass       getTimerBypass()       { return timerBypass; }
    public Phase             getPhase()             { return phase; }

    public List<Module> getAll() { return all; }
}
