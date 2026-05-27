package com.example

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

class AppOpenAdManager(private val context: Context) {
    private var appOpenAd: AppOpenAd? = null
    private var isShowingAd = false
    private val adUnitId = "ca-app-pub-4817345864795296/3993398124" // The user's provided ad unit ID
    
    fun loadAd(onLoaded: () -> Unit, onError: () -> Unit) {
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            adUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    onLoaded()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e("AppOpenAdManager", "Failed to load ad: ${loadAdError.message}")
                    onError()
                }
            }
        )
    }

    fun showAdIfAvailable(activity: Activity, onShowComplete: () -> Unit) {
        if (isShowingAd) {
            return
        }

        appOpenAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAd = false
                    onShowComplete()
                    // Optionally load the next ad here
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    isShowingAd = false
                    onShowComplete()
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingAd = true
                }
            }
            ad.show(activity)
        } ?: run {
            onShowComplete()
        }
    }
}
