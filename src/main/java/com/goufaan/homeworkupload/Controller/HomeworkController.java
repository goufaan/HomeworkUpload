package com.goufaan.homeworkupload.Controller;

import com.goufaan.homeworkupload.Misc;
import com.goufaan.homeworkupload.Model.Homework;
import com.goufaan.homeworkupload.Model.ResponseModel;
import com.goufaan.homeworkupload.Repository.IAuthRepository;
import com.goufaan.homeworkupload.Repository.IHomeworkRepository;
import com.goufaan.homeworkupload.Repository.ISubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public class HomeworkController {

    @Autowired
    IAuthRepository auth;
    @Autowired
    IHomeworkRepository homew;
    @Autowired
    ISubmissionRepository sub;

    @RequestMapping("/api/getlist")
    public ResponseModel GetHomeworkList() {
        var r = new ResponseModel(200);
        var result = homew.GetAllHomework();
        var dat = new ArrayList<HashMap<String,Object>>();
        for (var item : result){
            var hm = new HashMap<String,Object>();
            hm.put("id", item.getId());
            hm.put("name", item.getName());
            hm.put("owner", auth.GetUser(item.getOwner()).getUserName());
            var c = Calendar.getInstance();
            c.setTime(item.getDeadline());
            hm.put("deadline_format", Misc.getTimeFormat(c));
            dat.add(hm);
        }
        r.setData(dat);
        return r;
    }
    @RequestMapping("/api/auth/getlist")
    public ResponseModel GetMyHomeworkList(HttpServletRequest request) {
        var r = new ResponseModel(200);
        var result = homew.GetMyAllHomework(auth.GetLoginAs(request).getUid());
        var dat = new ArrayList<HashMap<String,Object>>();
        for (var item : result){
            var hm = new HashMap<String,Object>();
            hm.put("id", item.getId());
            hm.put("name", item.getName());
            hm.put("createDate", item.getCreateDate());
            dat.add(hm);
        }
        r.setData(dat);
        return r;
    }

    @RequestMapping("/api/get/{id}")
    public ResponseModel GetHomework(@PathVariable Integer id) {
        if (id == null)
            return new ResponseModel(1000);
        var result = homew.GetHomework(id);
        if (result == null)
            return new ResponseModel(3000);
        var r = new ResponseModel(200);
        var map = new HashMap<String,Object>();
        map.put("name",result.getName());
        map.put("createDate",result.getCreateDate());
        map.put("deadline",result.getDeadline());
        var c = Calendar.getInstance();
        c.setTime(result.getDeadline());
        map.put("deadline_format", Misc.getTimeFormat(c));
        map.put("sLimit",result.getSubmissionLimit());
        map.put("owner",auth.GetUser(result.getOwner()).getUserName());
        map.put("format",result.getSupportType());
        var subs = sub.GetAllSubmission(id);
        map.put("count", subs.size());
        map.put("fnExample",result.getFileNameExample());
        var p = new ArrayList<HashMap<String,Object>>();
        for (var item : subs){
            var hm = new HashMap<String,Object>();
            hm.put("name", item.getUser());
            hm.put("time", item.getCreateDate());
            p.add(hm);
        }
        map.put("submitted", p);
        r.setData(map);
        return r;
    }

    @RequestMapping("/api/auth/newhomework")
    public ResponseModel NewHomework(String name, @RequestParam(value = "stype") String[] stype, String fnExample, Integer sLimit, String fnFormat ,
                                     @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date deadline, HttpServletRequest request) {
        if (name == null || stype == null || fnExample == null || sLimit == null || fnFormat == null || deadline == null)
            return new ResponseModel(1000);
        var user = auth.GetLoginAs(request);
        var m = new Homework();
        m.setName(name);
        m.setOwner(user.getUid());
        m.setSupportType(stype);
        m.setFileNameExample(fnExample);
        m.setSubmissionLimit(sLimit);
        m.setFileNameFormat(fnFormat);
        m.setDeadline(deadline);
        m.setCreateDate(new Date());
        var ret = homew.AddHomework(m);
        if (ret != 200)
            return new ResponseModel(ret);
        var result = new ResponseModel("新增作业成功！");
        result.setData(m.getId());
        return result;
    }

    @RequestMapping("/api/auth/ismyhomework")
    public ResponseModel IsMyHomework(Integer hid, HttpServletRequest request) {
        if (hid == null)
            return new ResponseModel(1000);
        var result = new ResponseModel(200);
        result.setData(homew.IsMyHomework(hid, auth.GetLoginAs(request).getUid()));
        return result;
    }

    @RequestMapping("/api/auth/deletehomework")
    public ResponseModel DeleteHomework(Integer hid, HttpServletRequest request){
        if (hid == null)
            return new ResponseModel(1000);
        if (!homew.IsMyHomework(hid, auth.GetLoginAs(request).getUid()))
            return new ResponseModel(4000);
        return new ResponseModel(homew.RemoveHomework(hid),"删除成功");
    }
}