package com.oklb.braintrainingsystem.fragments

import android.app.Dialog
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.oklb.braintrainingsystem.R
import kotlinx.android.synthetic.main.fragment_braintraining_option.*
import kotlinx.android.synthetic.main.fragment_braintraining_play.*
import java.util.*
import kotlin.collections.ArrayList

class BrainTrainingFragment : Fragment(), BrainTrainingOptionFragment.OnReadyListener, BrainTrainingPlayFragment.OnReturnListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_brain_training, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity!!.supportFragmentManager.beginTransaction().apply {
            replace(R.id.brainTrainingFragmentArea, BrainTrainingOptionFragment().apply {
                onReadyListener = this@BrainTrainingFragment
            })
            commit()
        }
    }

    override fun onReady(skinId: Int, difficulty: Int) {
        activity!!.supportFragmentManager.beginTransaction().apply {
            replace(R.id.brainTrainingFragmentArea, BrainTrainingPlayFragment().apply {
                this.skinId = skinId
                this.difficulty = difficulty
                onReturnListener = this@BrainTrainingFragment
            })
            commit()
        }
    }

    override fun onReturn(skinId: Int, difficulty: Int) {
        activity!!.supportFragmentManager.beginTransaction().apply {
            replace(R.id.brainTrainingFragmentArea, BrainTrainingOptionFragment().apply {
                this.skinId = skinId
                this.difficulty = difficulty + 1
                onReadyListener = this@BrainTrainingFragment
            })
            commit()
        }
    }
}

class BrainTrainingOptionFragment : Fragment() {

    var height: Int = 0
    var width: Int = 0

    public lateinit var onReadyListener: OnReadyListener

    public var skinId = 0
    public var difficulty = 5

    private val menus =
        List(ImageSkin.allImageList.size) { i -> OptionListData(ImageSkin.titles[i], ImageSkin.allImageList[i][0]) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_braintraining_option, container, false)
        root.post {
            height = root.measuredHeight
            width = root.measuredWidth
            initialize()
        }
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ImageSkin.setSkinId(skinId)
        brainTrainingOptionButton01.setOnClickListener {
            onReadyListener.onReady(skinId, difficulty - 1)
        }
        val optionListAdapter = OptionListAdapter(this.context!!, menus)
        optionListView1.adapter = optionListAdapter

        optionListView1.setOnItemClickListener { parent, view, position, id ->
            for (v in parent.touchables) v.setBackgroundResource(R.drawable.colored_rectangler_00)
            parent.getChildAt(position).setBackgroundResource(R.drawable.colored_rectangler_01)
            skinId = id.toInt()
            ImageSkin.setSkinId(skinId)
            setComponents()
        }
        optionMinusButton.setOnClickListener {
            if (difficulty > 1) difficulty--
            difficultyTextView.text = "" + difficulty
        }
        optionPlusButton.setOnClickListener {
            if (difficulty < 9) difficulty++
            difficultyTextView.text = "" + difficulty
        }
        difficultyTextView.text = "" + difficulty
    }

    private fun initialize() {
        val scale = previewLayout01.width
        Log.d("debug", "scale = $scale")
        previewImageView01.layoutParams.height = scale / 3
        previewImageView01.layoutParams.width = scale / 3
        previewImageView02.layoutParams.height = scale / 3
        previewImageView02.layoutParams.width = scale / 3
        previewImageView03.layoutParams.height = scale / 3
        previewImageView03.layoutParams.width = scale / 3
        previewImageView04.layoutParams.height = scale / 3
        previewImageView04.layoutParams.width = scale / 3
        setComponents()
    }

    private fun setComponents() {
        previewImageView01.setImageResource(ImageSkin.images[0])
        previewImageView02.setImageResource(ImageSkin.images[1])
        previewImageView03.setImageResource(ImageSkin.images[2])
        previewImageView04.setImageResource(ImageSkin.images[3])
    }

    public interface OnReadyListener {
        public fun onReady(skinId: Int, difficulty: Int)
    }

    data class OptionListData(val title: String, val imageId: Int)

    data class ViewHolder(val optionTextView: TextView, val optionImageView: ImageView)

    class OptionListAdapter(context: Context, menus: List<OptionListData>) :
        ArrayAdapter<OptionListData>(context, 0, menus) {
        private val layoutInflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var viewHolder: ViewHolder?
            var view = convertView
            if (view == null) {
                view = layoutInflater.inflate(R.layout.list_items_2, parent, false)
                viewHolder = ViewHolder(view.findViewById(R.id.optionTextView), view.findViewById(R.id.optionImageView))
                view.tag = viewHolder
            } else {
                viewHolder = view.tag as ViewHolder
            }
            val listItem = getItem(position)
            viewHolder!!.optionTextView.text = listItem!!.title
            viewHolder!!.optionImageView.setImageBitmap(BitmapFactory.decodeResource(context.resources, listItem.imageId))
            return view!!
        }
    }

}

class BrainTrainingPlayFragment : Fragment(), SelectAnswerDialog.Companion.OnSelectListener {

    lateinit var buttonList: List<ImageView>
    lateinit var buttonCoverList: List<ImageView>
    lateinit var buttonMaskList: List<ImageView>

    lateinit var location: ArrayList<Int>
    lateinit var questions: ArrayList<Int>
    lateinit var answers: ArrayList<Int>

    val tileSize = listOf(4, 4, 4, 8, 8, 8, 12, 12, 12)
    val blankSize = listOf(1, 2, 3, 2, 4, 6, 3, 6, 9)

    var difficulty = 0
    var skinId = 0

    lateinit var brainTrainingParameter: BrainTrainingParameter

    private var height = 0
    private var width = 0

    public lateinit var onReturnListener: OnReturnListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_braintraining_play, container, false)
        root.post {
            height = root.measuredHeight
            width = root.measuredWidth
            Log.d("debug", "h: $height, w: $width")
            setComponentSize()
            initialize(true)
        }
        return root
    }

    private fun setComponentSize() {
        val scale = height
        buttonList = listOf<ImageView>(
            ImageView01, ImageView02, ImageView03, ImageView04, ImageView05, ImageView06,
            ImageView07, ImageView08, ImageView09, ImageView10, ImageView11, ImageView12
        )
        buttonCoverList = listOf<ImageView>(
            coverImageView01, coverImageView02, coverImageView03, coverImageView04, coverImageView05, coverImageView06,
            coverImageView07, coverImageView08, coverImageView09, coverImageView10, coverImageView11, coverImageView12
        )
        buttonMaskList = listOf<ImageView>(
            maskImageView01, maskImageView02, maskImageView03, maskImageView04, maskImageView05, maskImageView06,
            maskImageView07, maskImageView08, maskImageView09, maskImageView10, maskImageView11, maskImageView12
        )
        rowLayout01.layoutParams.height = scale / 3
        rowLayout01.layoutParams.width = scale / 3 * 4
        rowLayout02.layoutParams.height = scale / 3
        rowLayout02.layoutParams.width = scale / 3 * 4
        rowLayout03.layoutParams.height = scale / 3
        rowLayout03.layoutParams.width = scale / 3 * 4
        for (view in buttonList) {
            view.layoutParams.height = scale / 3
            view.layoutParams.width = scale / 3
        }
        for (view in buttonCoverList) {
            view.layoutParams.height = scale / 3
            view.layoutParams.width = scale / 3
        }
        for (view in buttonMaskList) {
            view.layoutParams.height = scale / 3
            view.layoutParams.width = scale / 3
        }
    }

    private fun initialize(isReset: Boolean) {
        location = generateLocation(isReset)
        questions = generateQuestions(isReset)
        answers = generateAnswers(isReset)
        brainTrainingParameter = generateBrainTrainingParameter(isReset)
        viewInitialize()
        memorize()
    }

    private fun generateLocation(isReset: Boolean): ArrayList<Int> {
        val newLocation = ArrayList<Int>()
        val r = Random()
        for (i in 0 until 12) {
            if (i < tileSize[difficulty]) {
                newLocation.add(r.nextInt(4))
            } else {
                newLocation.add(-1)
            }
        }
        return if (isReset) newLocation else location
    }

    private fun generateQuestions(isReset: Boolean): ArrayList<Int> {
        val newQuestion = arrayListOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        if (isReset) {
            val r = Random()
            var sum = 0
            while (sum < blankSize[difficulty]) {
                val index = r.nextInt(tileSize[difficulty])
                if (newQuestion[index] == 0) {
                    newQuestion[index] = 1
                    sum++
                }
            }
        } else {
            for (i in 0 until 12) {
                if (location[i] != answers[i]) newQuestion[i] = 1
            }
        }
        return newQuestion
    }

    private fun generateAnswers(isReset: Boolean): ArrayList<Int> {
        val newAnswers = ArrayList<Int>()
        for (i in 0 until location.size) {
            if (questions[i] == 0) newAnswers.add(location[i]) else newAnswers.add(-1)
        }
        return newAnswers
    }

    private fun generateBrainTrainingParameter(isReset: Boolean): BrainTrainingParameter {
        var brainTrainingParameter: BrainTrainingParameter
        if (isReset) {
            brainTrainingParameter = BrainTrainingParameter(
                Calendar.getInstance(),
                Calendar.getInstance(),
                skinId,
                difficulty,
                0,
                tileSize[difficulty],
                blankSize[difficulty],
                -1,
                location,
                questions,
                answers
            )
        } else {
            var b = 0
            for (i in 0 until 12) {
                b += questions[i]
            }
            brainTrainingParameter = BrainTrainingParameter(
                Calendar.getInstance(),
                Calendar.getInstance(),
                skinId,
                difficulty,
                this.brainTrainingParameter.retry + 1,
                tileSize[difficulty],
                b,
                -1,
                location,
                questions,
                answers
            )
        }
        return brainTrainingParameter
    }

    private fun viewInitialize() {
        for (view in buttonList) {
            view.setImageResource(0)
            view.clearAnimation()
        }
        for (view in buttonCoverList) {
            view.setImageResource(0)
            view.clearAnimation()
            view.setOnClickListener(null)
        }
        for (view in buttonMaskList) {
            view.setImageResource(0)
            view.clearAnimation()
        }
        gameReturnButton.text = ""
        gameGotoResultButton.text = ""
        scoreTextView.text = ""
    }

    private fun memorize() {
        for (i in 0 until tileSize[difficulty]) {
            val view = buttonList[i]
            view.setImageResource(ImageSkin.images[brainTrainingParameter.location[i]])
        }
        for (view in buttonCoverList) {
            view.setImageResource(0)
            view.clearAnimation()
            view.setOnClickListener(null)
        }
        for (view in buttonMaskList) {
            view.setImageResource(0)
            view.clearAnimation()
        }
        gameReturnButton.setOnClickListener {
            onReturnListener.onReturn(skinId, difficulty)
        }
        gameGotoResultButton.setOnClickListener {
            reply()
        }
        gameReturnButton.text = "戻る"
        gameGotoResultButton.text = "覚えた！"
        scoreTextView.text = ""
    }

    private fun reply() {
        for (i in 0 until tileSize[difficulty]) {
            val view = buttonCoverList[i]
            if (brainTrainingParameter.answers[i] == -1) {
                buttonList[i].setImageResource(0)
                view.setImageResource(ImageSkin.emptyAnswer)
                view.setOnClickListener {
                    Log.d("debug", "I am : $i")
                    val selectAnswerDialog = SelectAnswerDialog()
                    selectAnswerDialog.onSelectListener = this
                    selectAnswerDialog.pos = i
                    selectAnswerDialog.show(fragmentManager, "test")
                }
                val alphaAnimation = AlphaAnimation(1f, 0.5f)
                alphaAnimation.duration = 1500
                alphaAnimation.repeatCount = Animation.INFINITE
                alphaAnimation.repeatMode = Animation.REVERSE
                view.startAnimation(alphaAnimation)
            }
        }
        gameReturnButton.setOnClickListener {
            memorize()
        }
        gameGotoResultButton.setOnClickListener {
            result()
        }
        gameReturnButton.text = "戻る"
        gameGotoResultButton.text = "答え合わせ"
        scoreTextView.text = ""
    }

    private fun result() {
        brainTrainingParameter.finishedCalendar = Calendar.getInstance()
        for (view in buttonCoverList) {
            view.setOnClickListener(null)
        }
        var correct = 0
        for (i in 0 until tileSize[difficulty]) {
            if (questions[i] == 1) {
                if (location[i] == answers[i]) {
                    correct++
                    buttonMaskList[i].setImageResource(ImageSkin.tureAnswer)
                    val alphaAnimation = AlphaAnimation(1f, 0.5f)
                    alphaAnimation.duration = 1500
                    alphaAnimation.repeatCount = Animation.INFINITE
                    alphaAnimation.repeatMode = Animation.REVERSE
                    buttonMaskList[i].startAnimation(alphaAnimation)
                } else {
                    buttonMaskList[i].setImageResource(ImageSkin.falseAnswer)
                    buttonList[i].setImageResource(ImageSkin.images[location[i]])
                    buttonMaskList[i].startAnimation(AlphaAnimation(1f, 0.1f).apply {
                        duration = 1500
                        repeatCount = Animation.INFINITE
                        repeatMode = Animation.REVERSE
                    })
                    buttonCoverList[i].startAnimation(AlphaAnimation(1f, 0.1f).apply {
                        duration = 1500
                        repeatCount = Animation.INFINITE
                        repeatMode = Animation.REVERSE
                    })
                }
            }
        }
        scoreTextView.text = "" + correct + "/" + brainTrainingParameter.blank
        brainTrainingParameter.correct = correct
        brainTrainingParameter.finishedCalendar = Calendar.getInstance()
        Log.d("debug", "correct : $correct, blanks : ${brainTrainingParameter.blank}")
        Log.d("debug", "${brainTrainingParameter.toString()}")

        gameReturnButton.text = "設定に戻る"
        gameReturnButton.setOnClickListener {
            onReturnListener.onReturn(skinId, difficulty)
        }
        if (correct == brainTrainingParameter.blank) {
            gameGotoResultButton.text = "もう一度"
            gameGotoResultButton.setOnClickListener {
                initialize(true)
            }
        } else {
            gameGotoResultButton.text = "再挑戦"
            gameGotoResultButton.setOnClickListener {
                initialize(false)
            }
        }
    }

    override fun onSelected(pos: Int, id: Int) {
        answers[pos] = id
        buttonCoverList[pos].setImageResource(ImageSkin.images[id])
        buttonCoverList[pos].clearAnimation()
    }

    public interface OnReturnListener {
        public fun onReturn(skinId: Int, difficulty: Int)
    }
}

data class GameConfiguration(var tmp: Int)

data class BrainTrainingParameter(
    var startCalendar: Calendar,
    var finishedCalendar: Calendar,
    var skinId: Int,
    var difficulty: Int,
    var retry: Int,
    var total: Int,
    var blank: Int,
    var correct: Int,
    var location: ArrayList<Int>,
    var questions: ArrayList<Int>,
    var answers: ArrayList<Int>
)

class ImageSkin {
    companion object {
        public val allImageList = arrayListOf(
            listOf(
                R.drawable.circle, R.drawable.rectangler, R.drawable.star, R.drawable.triangle
            ),
            listOf(
                R.drawable.flower_1, R.drawable.flower_2, R.drawable.flower_3, R.drawable.flower_4
            ),
            listOf(
                R.drawable.onigiri_seachicken,
                R.drawable.onigiri_yakionigiri,
                R.drawable.onigiri_takana,
                R.drawable.onigiri_yukari
            ),
            listOf(
                R.drawable.onigiri_ume,
                R.drawable.onigiri_tenmusu,
                R.drawable.onigiri_tarako,
                R.drawable.onigiri_seachicken
            )
        )

        public val titles = listOf("図形", "花々", "おにぎり", "おむすび")

        public var images = allImageList[0]

        public val tureAnswer = R.drawable.true_answer
        public val falseAnswer = R.drawable.false_answer
        public val emptyAnswer = R.drawable.empty_answer

        public fun setSkinId(id: Int) {
            images = allImageList[id]
        }
    }
}

class SelectAnswerDialog : DialogFragment() {

    companion object {
        public interface OnSelectListener {
            public fun onSelected(pos: Int, id: Int)
        }
    }

    lateinit var onSelectListener: OnSelectListener

    var pos = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity!!)
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )
        dialog.setContentView(R.layout.dialog_select_answer)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        //val buttonList = listOf<ImageView>(dialogImageView1, dialogImageView2, dialogImageView3, dialogImageView4)
        val buttonList = listOf<ImageView>(
            dialog.findViewById(R.id.dialogImageView1),
            dialog.findViewById(R.id.dialogImageView2),
            dialog.findViewById(R.id.dialogImageView3),
            dialog.findViewById(R.id.dialogImageView4)
        )

        for (i in 0 until buttonList.size) {
            val view = buttonList[i]
            view.setImageResource(ImageSkin.images[i])
            Log.d("debug", "dialog $i ok")
            view.setOnClickListener {
                Log.d("debug", "dialog $i clicked!!")
                onSelectListener.onSelected(pos, i)
                dismiss()
            }
        }

        return dialog
    }
}
