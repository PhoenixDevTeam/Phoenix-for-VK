package biz.dealnote.messenger

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import biz.dealnote.messenger.util.RxUtils
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

fun <T : Any> Single<T>.fromIOToMain(): Single<T> = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

fun <T : Any> Single<T>.subscribeIOAndIgnoreResults(): Disposable = subscribeOn(Schedulers.io()).subscribe(RxUtils.ignore(), RxUtils.ignore())

fun <T : Any> Flowable<T>.toMainThread(): Flowable<T> = observeOn(AndroidSchedulers.mainThread())

fun <T : Any> Observable<T>.toMainThread(): Observable<T> = observeOn(AndroidSchedulers.mainThread())

fun Completable.fromIOToMain(): Completable = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

fun SQLiteDatabase.query(tablename: String, columns: Array<String>, where: String?, args: Array<String>?): Cursor = query(tablename, columns, where, args, null, null, null)

fun SQLiteDatabase.query(tablename: String, columns: Array<String>): Cursor = query(tablename, columns, null, null)

fun Cursor.getNullableInt(columnName: String): Int? = getColumnIndex(columnName).let { if (isNull(it)) null else getInt(it) }

fun Cursor.getInt(columnName: String): Int = getInt(getColumnIndex(columnName))

fun Cursor.getBoolean(columnName: String): Boolean = getInt(getColumnIndex(columnName)) == 1

fun Cursor.getLong(columnName: String): Long? = getColumnIndex(columnName).let { if (isNull(it)) null else getLong(it) }

fun Cursor.getString(columnName: String): String? = getString(getColumnIndex(columnName))

fun <T: Any> Collection<T>?.nullOrEmpty(): Boolean = if(this == null) true else size == 0

fun <T: Any> Collection<T>?.nonEmpty(): Boolean = if(this == null) false else size > 0

fun CharSequence?.nonEmpty(): Boolean = if(this == null) false else length > 0

fun <T : Any> Flowable<T>.subscribeIgnoreErrors(consumer: Consumer<in T>): Disposable = subscribe(consumer, RxUtils.ignore())

fun <T : Any> Single<T>.subscribeIgnoreErrors(consumer: Consumer<in T>): Disposable = subscribe(consumer, RxUtils.ignore())

fun Completable.subscribeIOAndIgnoreResults(): Disposable = subscribeOn(Schedulers.io()).subscribe(RxUtils.dummy(), RxUtils.ignore())