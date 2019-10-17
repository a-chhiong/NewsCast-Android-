package com.unilarm.newscast.orphanage;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/** https://blog.csdn.net/lengguoxing/article/details/42145641 */

public abstract class MyActivityManager
{
    public static List<Activity> myActivityList = new ArrayList<>();

    /** Add certain Activity into a container */

    public static void addActivity(Activity anyActivity)
    {
        if (! myActivityList.contains(anyActivity))
        {
            myActivityList.add(anyActivity);
        }
    }


    /** Finish certain Activity by its realised Class */

    public static void finishActivity(Class<?> anyClass)
    {
        if(anyClass != null)
        {
            Activity thisActivity = null;

            for (Activity anyActivity : myActivityList)
            {
                if (anyActivity.getClass().equals(anyClass))
                {
                    thisActivity = anyActivity;

                    break;
                }
            }

            finishActivity(thisActivity);
        }
    }

    /** Finish certain Activity */

    private static void finishActivity(Activity anyActivity)
    {
        if (anyActivity != null)
        {
            if (myActivityList.contains(anyActivity))
            {
                myActivityList.remove(anyActivity);
            }

            anyActivity.finish();
        }
    }
}


