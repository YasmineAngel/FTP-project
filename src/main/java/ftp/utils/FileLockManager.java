package ftp.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.nio.file.Path;

public class FileLockManager {
    private static final FileLockManager INSTANCE = new FileLockManager();
    private final ConcurrentHashMap<Path, ReadWriteLock> locks = new ConcurrentHashMap<>();
    private FileLockManager() {}

    public static FileLockManager getInstance() {
        return INSTANCE;
    }
    private ReadWriteLock getLock(Path path) {
        return locks.computeIfAbsent(path.toAbsolutePath(), k -> new ReentrantReadWriteLock());
    }

    public void acquireReadLock(Path path) {
        getLock(path).readLock().lock();
    }

    public void releaseReadLock(Path path) {
        getLock(path).readLock().unlock();
    }

    public void acquireWriteLock(Path path) {
        getLock(path).writeLock().lock();
    }

    public void releaseWriteLock(Path path) {
        getLock(path).writeLock().unlock();
    }
}