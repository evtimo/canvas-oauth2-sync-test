package com.canvas.sync.dao.store.impl;

import com.canvas.sync.dao.entity.AccountEntity;
import com.canvas.sync.dao.store.AccountStore;
import org.springframework.stereotype.Repository;

@Repository
public class AccountStoreImpl extends BaseStoreImpl<AccountEntity> implements AccountStore {

    public AccountStoreImpl() {
        super(AccountEntity.class);
    }
}
