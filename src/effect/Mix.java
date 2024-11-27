package src.effect;


import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.beans.Transient;
import java.io.File;
import java.io.IOException;

import src.base.Effect;
import src.draw.ModLoader;


/****************************************
 *  ミキサー
 */
public class Mix extends Effect {

	private static final long serialVersionUID = 3746115855650888135L;
	public static BufferedImage[] images;
	public static int imgW;
	public static int imgH;
	public static int pivX;
	public static int pivY;
	/**
	 * イメージをロードする.
	 * @param loader ローダ
	 * @param io イメージオブザーバ
	 * @throws IOException IO例外
	 */
	public static void loadImages (ClassLoader loader, ImageObserver io) throws IOException {
		
		images = new BufferedImage[3];
		for(int i = 0; i < 3; i++) {
			images[i] = ModLoader.loadItemImage(loader, "effect" + File.separator + "mix_" + i + ".png");
		}

		imgW = images[0].getWidth(io);
		imgH = images[0].getHeight(io);
		pivX = imgW >> 1;
		pivY = imgH - 1;
	}
	/**
	 * イメージを取得する.
	 */
	@Override
	@Transient
	public BufferedImage getImage() {
		return images[animeFrame];
	}
	/**
	 * コンストラクタ.
	 */
	public Mix(int sX, int sY, int sZ, int vX, int vY, int vZ, boolean invert,
						int life, int loop, boolean end, boolean grav, boolean front) {
		super(sX, sY, sZ, vX, vY, vZ, invert, life, loop, end, grav, front);
		setBoundary(pivX, pivY, imgW, imgH);
		interval = 0;
		frames = 3;
	}
	
	public Mix() {
		
	}
	public static BufferedImage[] getImages() {
		return images;
	}
	public static void setImages(BufferedImage[] images) {
		Mix.images = images;
	}
}
