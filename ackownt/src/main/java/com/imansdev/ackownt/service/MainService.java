package com.imansdev.ackownt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.imansdev.ackownt.repository.AccountsRepository;
import com.imansdev.ackownt.repository.TransactionsRepository;
import com.imansdev.ackownt.repository.UsersRepository;

@Service
public class MainService {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private AccountsRepository accountsRepository;
    @Autowired
    private TransactionsRepository transactionsRepository;
}
