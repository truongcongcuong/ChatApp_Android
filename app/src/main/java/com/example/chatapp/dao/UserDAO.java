//package com.example.chatapp.dao;
//
//import androidx.room.Dao;
//import androidx.room.Delete;
//import androidx.room.Insert;
//import androidx.room.Query;
//import androidx.room.Update;
//
//import com.example.chatapp.entity.User;
//
//import java.util.List;
//
//@Dao
//public interface UserDAO {
//    @Query("SELECT * FROM user")
//    List<User> getAll();
//
//    @Insert
//    void insertAll(User... users);
//
//    @Delete
//    void delete(User user);
//
//    @Query("delete from user")
//    void deleteAll();
//
//    @Update
//    void update(User... userSummaryDTOS);
//
//}
