package com.sharesdk.analytics.data;

import java.util.Random;


public class Constant {

	static Random random = new Random();
	private static final String[] STRARRARY = {"purchase", "play", "shopping", "swimming", "running", "fishing", "traveling", "reading", "working", "eating"
		                                       , "tomato", "cabbage", "potato", "carrot", "bean", "spinach", "tofu", "lettuce", "pig", "cat", "dog", "hen", "cow", "duck", "goose"
		                                       , "rabit", "snake", "tiger", "goat", "leopard", "lion", "elephant", "camel", "donkey", "bird", "shark", "dolphin", "whale", "seagull", "penguin"};
	
	private static String getEvent(int i){
		return STRARRARY[i];
	}
	
	public static int getRandomNum(){
		return Math.abs(random.nextInt())%25;
	}
	
	public static String getRandomEventEnd(){
		return getEvent(Math.abs(random.nextInt())%40);
	}
	
	public static String getRandomNum(int model){
		return String.valueOf(Math.abs(random.nextInt())%model);
	}
	
}
