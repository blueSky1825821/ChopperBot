package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.bean.FocusLiver;

import java.util.List;

public interface FocusLiverService extends IService<FocusLiver> {

    List<FocusLiver> getFocusLivers();

    List<FocusLiver> getFocusLivers(String platform);

    boolean deleteLivers(String platform,String liver);

    boolean addLivers(FocusLiver liver);

    boolean hasLiver(String platform,String liver);

    boolean updateLivers(FocusLiver liver);
}
