package com.adtsw.jcommons.utils.logging;

public interface Logger<E> {
    
    void log(E message);

    void flush();

    void shutdown();
}
