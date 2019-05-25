package com.goufaan.homeworkupload.Controller;

import com.goufaan.homeworkupload.Model.Admin;
import com.goufaan.homeworkupload.Model.ResponseModel;
import com.goufaan.homeworkupload.Repository.IAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AdminController {

    @Autowired
    IAuthRepository auth;

    @RequestMapping("/api/register")
    public String Register(){
        auth.Register("123456","123456","123@abc.com");
        return "ok!";
    }

    @RequestMapping("/api/signin")
    public ResponseModel SignIn(String userName, String password, HttpServletRequest request){
        if (userName == null || password == null)
            return new ResponseModel(1000);

        var result = auth.Login(userName, password);
        if (result == null)
            return new ResponseModel(2000);

        var s = request.getSession();
        s.setAttribute("OPENID", result);
        s.setMaxInactiveInterval(30 * 60);
        return new ResponseModel("登录成功");
    }

    @RequestMapping("/api/signout")
    public ResponseModel SignOut(HttpServletRequest request){
        request.getSession().removeAttribute("OPENID");
        return new ResponseModel("成功退出登录");
    }

    @RequestMapping("/api/auth/me")
    public Admin Me(HttpServletRequest request){
        return auth.GetLoginAs(request);
    }

}
