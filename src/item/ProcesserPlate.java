package src.item;


import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import src.SimYukkuri;
import src.attachment.Fire;
import src.base.Body;
import src.base.Effect;
import src.base.Obj;
import src.base.ObjEX;
import src.draw.ModLoader;
import src.draw.Translate;
import src.enums.CoreAnkoState;
import src.enums.CriticalDamegeType;
import src.enums.EffectType;
import src.enums.FootBake;
import src.enums.HairState;
import src.enums.Happiness;
import src.enums.ImageCode;
import src.enums.ObjEXType;
import src.system.Cash;
import src.system.MessagePool;

/***************************************************
加工プレート
*/
public class ProcesserPlate extends ObjEX implements java.io.Serializable {
	static final long serialVersionUID = 1L;

	public static final int hitCheckObjType = ObjEX.YUKKURI;
	private static BufferedImage[] images = new BufferedImage[2];
	private static Rectangle boundary = new Rectangle();

	private Random rnd = new Random();
	protected ArrayList<Body> processedBodyList = new ArrayList<Body>();
	protected ArrayList<Effect> processedBodyEffectList = new ArrayList<Effect>();
	protected ProcessType enumProcessType;
	protected int cost[] ={500,800,2000,3500};
	public static enum ProcessMode {
		HOTPLATE,		// ホットプレート
		PAIN,			// 痛い
		BAIBAI_OKAZARI,	// お飾り自動削除
		PEALING,		//皮むき
		BLINDING,		//	目潰し
		ACCELERATE,			//成長加速
		SHUTMOUTH,			//口封じ
		PLUCKING,			//むしる
		PACKING			//パッキング
	}

	public static enum ProcessType {
		HOTPLATE_MIN("加熱（最弱）",			ProcessMode.HOTPLATE,	50),
		HOTPLATE_LOW("加熱（弱火）",			ProcessMode.HOTPLATE,	500),
		HOTPLATE_MIDDLE("加熱（中火で狐色まで)",	ProcessMode.HOTPLATE,	1000),
		HOTPLATE_HIGH("加熱（強火）",			ProcessMode.HOTPLATE,	5000),
		HOTPLATE_MAX("加熱（最強）",			ProcessMode.HOTPLATE,	20000),
		PAIN("打撃",					ProcessMode.PAIN,		1),
		BAIBAI_OKAZARI_WITH_FIRE("お飾り自動除去",	ProcessMode.BAIBAI_OKAZARI,		1),
		PEALING("皮むき",	ProcessMode.PEALING,		1),
		BLINDING("目潰し",	ProcessMode.BLINDING,		1),
		ACCELERATE("成長加速",	ProcessMode.ACCELERATE,		1),
		SHUTMOUTH("口封じ",	ProcessMode.SHUTMOUTH,		1),
		PLUCKING("ハゲ饅頭",	ProcessMode.PLUCKING,		1),
		PACKING("饅頭生産機",	ProcessMode.PACKING,		1)
		;
		private String name;
		private ProcessMode eMode;
		private int nParam;
		ProcessType(String name, ProcessMode eMode, int nParam){
			this.name = name;
			this.eMode = eMode;
			this.nParam = nParam;
		}
	}


	public static void loadImages (ClassLoader loader, ImageObserver io) throws IOException {
		images[0] = ModLoader.loadItemImage(loader, "ProcesserPlate" + File.separator + "ProcesserPlate.png");
		images[1] = ModLoader.loadItemImage(loader, "ProcesserPlate" + File.separator + "ProcesserPlate_off.png");
		boundary.width = images[0].getWidth(io);
		boundary.height = images[0].getHeight(io);
		boundary.x = boundary.width >> 1;
		boundary.y = boundary.height >> 1;
	}

	@Override
	public int getImageLayer(BufferedImage[] layer) {
		if(enabled){
			layer[0] = images[0];
		}
		else{
			layer[0] = images[1];
		}
		return 1;
	}

	@Override
	public BufferedImage getShadowImage() {
		return null;
	}

	public static Rectangle getBounding() {
		return boundary;
	}

	@Override
	public int getHitCheckObjType() {
		return hitCheckObjType;
	}

	@Override
	public boolean enableHitCheck() {
		return true;
	}

	public boolean checkHitObj(Rectangle colRect, Obj o){
		if(o.getZ() == 0){
			Translate.translate(o.getX(), o.getY(), tmpPos);
			if(colRect.contains(tmpPos)){
					objHitProcess(o);
						return true;
			}
			else{
				if( o != null && processedBodyList.contains(o) ){
					if( o instanceof Body){
						Body bTarget = (Body)o;
						int nIndex = processedBodyList.indexOf(bTarget);
						Effect effect = processedBodyEffectList.get(nIndex);
						if(effect != null )	{
							effect.remove();
						}
						bTarget.setForceFace(-1);
						bTarget.setDropShadow(true);
						bTarget.setLockmove(false);
						bTarget.setForcePanicClear();
						processedBodyList.remove(bTarget);
						processedBodyEffectList.remove(effect);
					}
				}
			}
		}
	return false;
	}

	@Override
	public int objHitProcess( Obj o ) {
		if(!enabled) return 0;
		if( o == null ) return 0;
		if( o instanceof Body ){
			Body bTarget = (Body)o;
			// 切断されていたら何も起きない
			if(bTarget.getCriticalDamegeType() == CriticalDamegeType.CUT) return 0;
			// 初回
			if(!processedBodyList.contains(bTarget)){
				Effect effect;
				switch( enumProcessType.eMode){
					case HOTPLATE:
							effect = SimYukkuri.mypane.terrarium.addEffect(EffectType.BAKE, bTarget.getX(), bTarget.getY() + 1,
									-2, 0, 0, 0, false, -1, -1, false, false, false);
						break;
					default:
						// 空エフェクト
						effect = null;
						break;
				}
				processedBodyList.add(bTarget);
				processedBodyEffectList.add(effect);
			}
		}
		return 1;
	}

	@Override
	public void upDate() {
		if(!enabled){
			if( processedBodyList == null || processedBodyEffectList == null ){
				return;
			}
			for( int i=processedBodyList.size()-1; 0<=i; i-- ){
				Body bTarget = processedBodyList.get(i);
				Effect effect = processedBodyEffectList.get(i);
				if(effect != null ){
					effect.remove();
				}
				bTarget.setForceFace(-1);
				bTarget.setDropShadow(true);
				processedBodyList.remove(i);
				processedBodyEffectList.remove(i);
			}
			return ;
		}

		if ( getAge() % 2400 == 0 ){
			Cash.addCash(-getCost());
		}
		if( processedBodyList == null || processedBodyEffectList == null ){
			return;
		}
		for( int i=processedBodyList.size()-1; 0<=i; i-- ){
			Body bTarget = processedBodyList.get(i);
			Effect effect = processedBodyEffectList.get(i);

			//対象がいないor除去されたor飛んでいるときを除外
			if( bTarget == null || bTarget.isRemoved() || bTarget.getZ() >= 10){
				if(effect != null ){
					effect.remove();
				}
				bTarget.setForceFace(-1);
				bTarget.setDropShadow(true);
				processedBodyList.remove(i);
				processedBodyEffectList.remove(i);
				continue;
			}
			if( effect != null ){
				effect.setX(bTarget.getX());
				effect.setY(bTarget.getY()+2);
			}
			bTarget.clearActions();
			bTarget.setDropShadow(false);
			switch( enumProcessType.eMode){
				case HOTPLATE:
					if(!bTarget.isDead() ) {
						if(bTarget.isSleeping()) bTarget.wakeup();
						if( enumProcessType != ProcessType.HOTPLATE_MIDDLE || bTarget.getFootBakeLevel() != FootBake.MIDIUM){
							// 中火の場合は完全に足を焼かない
							bTarget.addFootBakePeriod(enumProcessType.nParam);
						}
						bTarget.addDamage(20);
						if(bTarget.isPealed())bTarget.addStress(400);
						else bTarget.addStress(40);
						if( bTarget.geteCoreAnkoState() == CoreAnkoState.DEFAULT ){
							bTarget.setHappiness(Happiness.VERY_SAD);
							bTarget.setForceFace(ImageCode.PAIN.ordinal());
							if(rnd.nextInt(10) == 0) {
								bTarget.setMessage(MessagePool.getMessage(bTarget, MessagePool.Action.Burning), 40, true, true);
							}
						}
					}
					break;
				case PAIN:
					if(!bTarget.isDead()) {
						if(bTarget.isSleeping()) bTarget.wakeup();
						bTarget.addDamage(5);
						if(bTarget.isPealed())bTarget.addStress(400);
						else bTarget.addStress(30);
						if( bTarget.geteCoreAnkoState() == CoreAnkoState.DEFAULT ){
							bTarget.setHappiness(Happiness.VERY_SAD);
							bTarget.setForceFace(ImageCode.PAIN.ordinal());
							if(rnd.nextInt(15) == 0) {
								bTarget.setMessage(MessagePool.getMessage(bTarget, MessagePool.Action.Scream), 40, true, true);
							}
						}
					}
					break;
				case BAIBAI_OKAZARI:// お飾り除去（燃やす）
					// 潰れていたらそのまま流す
					if( bTarget.isCrushed() ){
						break;
					}
					if( bTarget.hasOkazari() ){
						if(bTarget.getAttachmentSize(Fire.class) == 0){
							bTarget.setForcePanicClear();
							bTarget.giveFire();
						}
						bTarget.setLockmove(true);
						if(!bTarget.isDead()){
							// 死にそうなら回復する
							if ( bTarget.getDamage() >= bTarget.getDamageLimit()*60/100 ) {
								bTarget.addDamage( - TICK*100);
							}
						}
					}
					else{
						if(bTarget.getAttachmentSize(Fire.class) != 0){
							bTarget.removeAttachment(Fire.class, true);
							//bTarget.giveWater(); // 水をかけると赤ゆが高確率で死ぬのでOFF
							bTarget.setLockmove(false);
							bTarget.setForcePanicClear();
						}
					}
					break;
				case PEALING:
					//潰れ、加工済み除外
					if(bTarget.isCrushed() || bTarget.isPealed()){
						break;
					}
					//ゲームバランス調整用。お飾り付、おさげ付の個体は処理しない
					if(bTarget.hasOkazari() || (bTarget.hasBraidCheck() && bTarget.isBraidType())){
						break;
					}
					if(bTarget.isSleeping()) bTarget.wakeup();
					bTarget.cutHair();
					bTarget.Peal();
					/*if(bTarget.hasBraidCheck()){
						bTarget.takeBraid();
					}*/
					if(!bTarget.isDead()){
						// 死にそうなら回復する
						if ( bTarget.isDamagedHeavily() ) {
							bTarget.addDamage( - TICK*100);
						}
					}
					break;
				case BLINDING:
					//潰れ、加工済み除外
					if(bTarget.isCrushed() || bTarget.isBlind()){
						break;
					}
					if(bTarget.isSleeping()) bTarget.wakeup();
					bTarget.breakeyes();
					break;
				case ACCELERATE:
					//潰れ、死体の除外
					if(bTarget.isCrushed()|| bTarget.isDead()){
					break;
					}
					if(bTarget.isSleeping()) bTarget.wakeup();
					//赤、子ゆのみ
					if(!bTarget.isAdult()){
						bTarget.setHappiness(Happiness.VERY_SAD);
						bTarget.setForceFace(ImageCode.PAIN.ordinal());
						bTarget.addAge(TICK * 1000);
						bTarget.setMessage(MessagePool.getMessage(bTarget, MessagePool.Action.Inflation), 40, false, true);
					}
					break;
				case SHUTMOUTH:
					if( bTarget.isCrushed()||bTarget.isPealed() ){
						break;
					}
					if(!bTarget.isShutmouth()){
						if(bTarget.isSleeping()) bTarget.wakeup();
						bTarget.setMessage(MessagePool.getMessage(bTarget, MessagePool.Action.CantTalk), 40, true, true);
						bTarget.setHappiness(Happiness.SAD);
						bTarget.setShutmouth(true);
					}
					break;
				case PLUCKING:
					//潰れ、加工済み除外
					if( bTarget.isCrushed() || bTarget.geteHairState() == HairState.BALDHEAD){
						break;
					}
					//ゲームバランス調整用。お飾り付は処理しない
					if(bTarget.hasOkazari()){
						break;
					}
					if(bTarget.isSleeping()) bTarget.wakeup();
					if(bTarget.hasBraidCheck()){
						bTarget.takeBraid();
					}
					bTarget.setHappiness(Happiness.VERY_SAD);
					bTarget.setForceFace(ImageCode.PAIN.ordinal());
					if(rnd.nextInt(3) == 0){
						bTarget.setMessage(MessagePool.getMessage(bTarget, MessagePool.Action.Scream), 40, true, true);
					}
					else{
					bTarget.setMessage(MessagePool.getMessage(bTarget, MessagePool.Action.PLUNCKING), 40, true, true);
					}
					bTarget.cutHair();
					break;
				case PACKING:
					//潰れ、死体、加工済み除外
					if(bTarget.isCrushed() || bTarget.isDead() || bTarget.isPacked()){
						break;
					}
					//ゲームバランス調整用。お飾り付、目有、髪付き、おさげ付、口未加工は処理しない
					if(bTarget.hasOkazari() || !bTarget.isBlind() || bTarget.geteHairState() != HairState.BALDHEAD || (bTarget.hasBraidCheck() && bTarget.isBraidType()) || !bTarget.isShutmouth()){
						break;
					}
					bTarget.pack();
					break;
				default:
					break;
			}
		}
	}

	@Override
	public int getCost(){
		switch( enumProcessType.eMode){
			case PAIN:
				return cost[0];
			case HOTPLATE:
			case ACCELERATE:
				return cost[1];
			case BAIBAI_OKAZARI:// お飾り除去（燃やす）
			case BLINDING:
			case SHUTMOUTH:
				return cost[2];
			case PEALING:
			case PLUCKING:
			case PACKING:
				return cost[3];
		}
		return 0;
	}

	@Override
	public void removeListData(){
		if( processedBodyList != null && processedBodyEffectList != null ){
			for( int i=processedBodyList.size()-1; 0<=i; i-- ){
				Body bTarget = processedBodyList.get(i);
				bTarget.setForceFace(-1);
				bTarget.setLockmove(false);
				Effect effect = processedBodyEffectList.get(i);
				if( effect != null ){
					effect.remove();
				}
			}
			processedBodyList.clear();
			processedBodyEffectList.clear();
		}
		SimYukkuri.world.currentMap.processerPlate.remove(this);
	}

	// 設定メニュー
	public static boolean setupProcesserPlate(ProcesserPlate plate) {

		JPanel mainPanel = new JPanel();
		JRadioButton[] but = new JRadioButton[ProcessType.values().length];
		boolean ret = false;

		mainPanel.setLayout(new GridLayout(7, 1));
		mainPanel.setPreferredSize(new Dimension(150, 100));
		ButtonGroup bg = new ButtonGroup();

		for(int i = 0; i < but.length; i++) {
			but[i] = new JRadioButton(ProcessType.values()[i].name);
			bg.add(but[i]);

			mainPanel.add(but[i]);
		}

		but[0].setSelected(true);

		int dlgRet = JOptionPane.showConfirmDialog(SimYukkuri.mypane, mainPanel, "加工設定", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		if(dlgRet == JOptionPane.OK_OPTION) {
			if(but[0].isSelected()) plate.enumProcessType = ProcessType.HOTPLATE_MIN;
			if(but[1].isSelected()) plate.enumProcessType = ProcessType.HOTPLATE_LOW;
			if(but[2].isSelected()) plate.enumProcessType = ProcessType.HOTPLATE_MIDDLE;
			if(but[3].isSelected()) plate.enumProcessType = ProcessType.HOTPLATE_HIGH;
			if(but[4].isSelected()) plate.enumProcessType = ProcessType.HOTPLATE_MAX;
			if(but[5].isSelected()) plate.enumProcessType = ProcessType.PAIN;
			if(but[6].isSelected()) plate.enumProcessType = ProcessType.BAIBAI_OKAZARI_WITH_FIRE;
			if(but[7].isSelected()) plate.enumProcessType = ProcessType.PEALING;
			if(but[8].isSelected()) plate.enumProcessType = ProcessType.BLINDING;
			if(but[9].isSelected()) plate.enumProcessType = ProcessType.ACCELERATE;
			if(but[10].isSelected()) plate.enumProcessType = ProcessType.SHUTMOUTH;
			if(but[11].isSelected()) plate.enumProcessType = ProcessType.PLUCKING;
			if(but[12].isSelected()) plate.enumProcessType = ProcessType.PACKING;
			ret = true;
		}
		return ret;
	}

	public ProcesserPlate(int initX, int initY, int initOption) {
		super(initX, initY, initOption);
		setBoundary(boundary);
		setCollisionSize(getPivotX(), getPivotY());
		SimYukkuri.world.currentMap.processerPlate.add(this);
		//objType = Type.PLATFORM;
		objEXType = ObjEXType.PROCESSERPLATE;
		interval = 5;
		value = 250000;
		readIniFile();
		boolean ret = setupProcesserPlate(this);
		if( !ret){
			SimYukkuri.world.currentMap.processerPlate.remove(this);
		}
	}

	public void readIniFile(){
		ClassLoader loader = this.getClass().getClassLoader();
		int nTemp = 0;
		//自動お仕置きプレートコスト
		nTemp = ModLoader.loadBodyIniMapForInt(loader, ModLoader.DATA_ITEM_INI_DIR, "ProcesserPlate", "MachineCost");
		cost[0] = nTemp;
		//軽加工プレートコスト
		nTemp = ModLoader.loadBodyIniMapForInt(loader, ModLoader.DATA_ITEM_INI_DIR, "ProcesserPlate", "LightProcessCost");
		cost[1] = nTemp;
		//中加工プレートコスト
		nTemp = ModLoader.loadBodyIniMapForInt(loader, ModLoader.DATA_ITEM_INI_DIR, "ProcesserPlate", "MidiumProcessCost");
		cost[2] = nTemp;
		//重加工プレートコスト
		nTemp = ModLoader.loadBodyIniMapForInt(loader, ModLoader.DATA_ITEM_INI_DIR, "ProcesserPlate", "HeavyProcessCost");
		cost[3] = nTemp;
	}

}


