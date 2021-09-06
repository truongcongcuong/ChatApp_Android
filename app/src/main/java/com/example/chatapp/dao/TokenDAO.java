//package com.example.chatapp.dao;
//
//import androidx.room.Dao;
//import androidx.room.Delete;
//import androidx.room.Insert;
//import androidx.room.Query;
//import androidx.room.Update;
//
//import com.example.chatapp.entity.Token;
//
//
//import java.util.List;
//
//@Dao
//public interface TokenDAO {
//    @Query("Select * from token")
//    List<Token> getAll();
//
//    @Insert
//    void insertAll(Token... tokens);
//
//    @Delete
//    void delete(Token token);
//
//    @Query("delete from token")
//    void deleteAll();
//
//    @Update
//    void update(Token... tokens);
//
//}
