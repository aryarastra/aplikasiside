package androidx.core.os;

import java.util.Locale;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public interface LocaleListInterface {
    Locale get(int index);

    Locale getFirstMatch(String[] supportedLocales);

    Object getLocaleList();

    int indexOf(Locale locale);

    boolean isEmpty();

    int size();

    String toLanguageTags();
}
