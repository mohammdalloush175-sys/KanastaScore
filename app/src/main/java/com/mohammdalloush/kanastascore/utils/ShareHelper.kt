package com.mohammdalloush.kanastascore.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.FileProvider
import com.mohammdalloush.kanastascore.R
import com.mohammdalloush.kanastascore.engine.SavedMatch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ShareHelper {

    fun shareMatchResult(context: Context, match: SavedMatch) {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_share_card, null)
        
        val tvMatchName = view.findViewById<TextView>(R.id.tv_share_match_name)
        val tvTeam1Name = view.findViewById<TextView>(R.id.tv_share_team1_name)
        val tvTeam1Total = view.findViewById<TextView>(R.id.tv_share_team1_total)
        val tvTeam2Name = view.findViewById<TextView>(R.id.tv_share_team2_name)
        val tvTeam2Total = view.findViewById<TextView>(R.id.tv_share_team2_total)
        val tvWinner = view.findViewById<TextView>(R.id.tv_share_winner)
        val tvRounds = view.findViewById<TextView>(R.id.tv_share_rounds)
        val tvDate = view.findViewById<TextView>(R.id.tv_share_date)

        val state = match.gameState
        val total1 = state.rounds.sumOf { it.team1Result.total }
        val total2 = state.rounds.sumOf { it.team2Result.total }

        tvMatchName.text = match.matchName
        tvTeam1Name.text = state.teams[0].name
        tvTeam1Total.text = total1.toString()
        tvTeam2Name.text = state.teams[1].name
        tvTeam2Total.text = total2.toString()
        
        val winnerName = if (total1 > total2) state.teams[0].name else state.teams[1].name
        tvWinner.text = context.getString(R.string.winner_is, winnerName)
        
        tvRounds.text = context.getString(R.string.rounds_count_label, state.rounds.size)
        
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvDate.text = sdf.format(Date(match.updatedAt))

        val bitmap = viewToBitmap(view)
        val uri = saveBitmapToCache(context, bitmap)
        if (uri != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_result)))
        }
    }

    private fun viewToBitmap(view: View): Bitmap {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        val imagesFolder = File(context.cacheDir, "images")
        imagesFolder.mkdirs()
        val file = File(imagesFolder, "match_result_${System.currentTimeMillis()}.png")
        
        // Clean up old images
        imagesFolder.listFiles()?.forEach { if (it.name.startsWith("match_result_")) it.delete() }

        return try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
