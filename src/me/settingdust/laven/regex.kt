package me.settingdust.laven

import java.util.regex.Matcher

operator fun Matcher.get(i: Int): String = group(i)