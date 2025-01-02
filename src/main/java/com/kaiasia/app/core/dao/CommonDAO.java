package com.kaiasia.app.core.dao;

import lombok.Data;

//Lớp cha cho các DAO
@Data
public abstract class CommonDAO {
    private String tableName;
}
