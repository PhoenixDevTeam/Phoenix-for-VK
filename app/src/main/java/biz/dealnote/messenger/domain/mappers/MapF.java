package biz.dealnote.messenger.domain.mappers;

/**
 * Created by admin on 3/19/2018.
 * Phoenix-for-VK
 */
public interface MapF<O, R> {
    R map(O orig);
}