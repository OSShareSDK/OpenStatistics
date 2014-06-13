/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sharesdk.example.analytics.data;

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
