package au.com.abhishek.android.rxkotlin.fragments

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.ListView
import au.com.abhishek.android.rxkotlin.R
import au.com.abhishek.android.rxkotlin.retrofit.Contributor
import au.com.abhishek.android.rxkotlin.retrofit.GithubService
import au.com.abhishek.android.rxkotlin.utils.unSubscribeIfNotNull
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.*

/**
 * Created by Abhishek Pathak on 19/04/2020.
 */
class PseudoCacheMergeFragment : BaseFragment() {

    //    @Bind(R.id.log_list) internal var _resultList: ListView

    private lateinit var _resultList: ListView
    private lateinit var _contributionMap: HashMap<String, Long>
    private lateinit var _adapter: ArrayAdapter<String>

    private var _subscription: Subscription? = null
    private val _resultAgeMap = HashMap<Contributor, Long>()

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _initializeCache()
        val layout = with(ctx) {
            verticalLayout {
                isBaselineAligned = false

                button("Start disk > network call") {
                    lparams(width = wrapContent, height = wrapContent) {
                        gravity = Gravity.CENTER
                        margin = dip(30)
                    }
                    onClick(onDemoPseudoCacheClicked)
                }
                _resultList = listView {
                    lparams(width = matchParent, height = wrapContent)
                }
            }
        }
        return layout
    }

    override fun onPause() {
        super.onPause()
        _subscription.unSubscribeIfNotNull()
    }

    val onDemoPseudoCacheClicked = { v: View? ->
        _adapter = ArrayAdapter(
            activity,
            R.layout.item_log,
            R.id.item_log,
            ArrayList<String>()
        )

        _resultList.adapter = _adapter
        _initializeCache()

        _subscription = Observable
            .merge(_getCachedData(), _getFreshData())
            .subscribeOn(Schedulers.io())
            .unsubscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { contributorAgePair ->
                    val contributor = contributorAgePair.first

                    if (_resultAgeMap.containsKey(contributor)
                        and
                        ((_resultAgeMap[contributor] ?: 0) > contributorAgePair.second)
                    ) {
                        return@subscribe
                    }

                    _contributionMap.put(contributor.login, contributor.contributions)
                    _resultAgeMap.put(contributor, contributorAgePair.second)

                    _adapter.clear()
                    _adapter.addAll(listStringFromMap)
                },
                {
                    Timber.e(it, "arr something went wrong")
                },
                {
                    Timber.d("done loading all data")
                }
            )
    }

    private val listStringFromMap: List<String>
        get() {
            val list = ArrayList<String>()

            for (username in _contributionMap.keys) {
                val rowLog = "$username [${_contributionMap[username]}]"
                list.add(rowLog)
            }

            return list
        }

    private fun _getCachedData(): Observable<Pair<Contributor, Long>> {

        val list = ArrayList<Pair<Contributor, Long>>()

        var dataWithAgePair: Pair<Contributor, Long>

        for (username in _contributionMap.keys) {
            val c = Contributor(
                login = username,
                contributions = _contributionMap[username] ?: 0
            )

            dataWithAgePair = Pair(c, System.currentTimeMillis())
            list.add(dataWithAgePair)
        }

        return Observable.from(list)
    }

    private fun _getFreshData(): Observable<Pair<Contributor, Long>> {
        val githubToken = resources.getString(R.string.github_oauth_token);
        val githubService = GithubService.createGithubService(githubToken)
        return githubService.contributors("square", "retrofit")
            .flatMap { contributors -> Observable.from(contributors) }
            .map { contributor -> Pair(contributor, System.currentTimeMillis()) }
    }

    private fun _initializeCache() {
        _contributionMap = HashMap<String, Long>()
        _contributionMap.put("JakeWharton", 0L)
        _contributionMap.put("pforhan", 0L)
        _contributionMap.put("edenman", 0L)
        _contributionMap.put("swankjesse", 0L)
        _contributionMap.put("bruceLee", 0L)
    }
}
