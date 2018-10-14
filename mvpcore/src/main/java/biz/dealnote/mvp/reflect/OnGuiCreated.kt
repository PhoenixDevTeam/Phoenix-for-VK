package biz.dealnote.mvp.reflect

/**
 * Created by ruslan.kolbasa on 05.10.2016.
 * phoenix
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class OnGuiCreated