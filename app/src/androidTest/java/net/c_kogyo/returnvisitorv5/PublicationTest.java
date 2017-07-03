package net.c_kogyo.returnvisitorv5;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import net.c_kogyo.returnvisitorv5.data.Publication;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import java.util.Calendar;

/**
 * Created by SeijiShii on 2017/02/20.
 */
@RunWith(AndroidJUnit4.class)
public class PublicationTest {

    @Test
    public void assertNumberString() {
        Context context = InstrumentationRegistry.getTargetContext();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, 0);
        String numberString = Publication.getNumberString(cal, Publication.MagazineCategory.AWAKE, context);
        assertEquals("Number String Test:", numberString, "No. 1 2017");
    }
}
