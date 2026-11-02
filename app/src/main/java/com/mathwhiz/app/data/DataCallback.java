package com.mathwhiz.app.data;

/** Simple success/error callback used by every repository. Always delivered on the main thread. */
public interface DataCallback<T> {
    void onSuccess(T value);

    void onError(String message);
}
