/*
 * Copyright 2020-2022 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

package com.goldenraven.devkitwallet.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.goldenraven.devkitwallet.data.Wallet
import com.goldenraven.devkitwallet.ui.Screen
import com.goldenraven.devkitwallet.ui.theme.DevkitWalletColors
import com.goldenraven.devkitwallet.ui.theme.firaMono
import com.goldenraven.devkitwallet.utilities.timestampToString
import org.bitcoindevkit.Transaction

@Composable
internal fun TransactionScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    txid: String?,
) {
    val transaction = getTransaction(txid = txid)
    if (transaction == null) {
        navController.popBackStack()
    }
    val transactionDetail = getTransactionDetails(transaction = transaction!!)

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(DevkitWalletColors.night4)
            .padding(paddingValues)
    ) {
        val (screenTitle, transactions, bottomButton) = createRefs()

        Column(
            modifier = Modifier
                .constrainAs(screenTitle) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(top = 70.dp)
        ) {
            Text(
                text = "Transaction",
                color = DevkitWalletColors.snow1,
                fontSize = 28.sp,
                fontFamily = firaMono,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = transactionTitle(transaction = transaction),
                color = DevkitWalletColors.snow1,
                fontSize = 14.sp,
                fontFamily = firaMono,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }


        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.constrainAs(transactions) {
                top.linkTo(screenTitle.bottom)
                bottom.linkTo(bottomButton.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                height = Dimension.fillToConstraints
            }
        ) {
            items(transactionDetail) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp)
                ) {
                    Text(
                        text = "${it.first} :",
                        fontSize = 16.sp,
                        fontFamily = firaMono,
                        color = DevkitWalletColors.snow1,
                    )
                    Text(
                        text = it.second,
                        fontSize = 16.sp,
                        fontFamily = firaMono,
                        textAlign = TextAlign.End,
                        color = DevkitWalletColors.snow1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 8.dp, horizontal = 8.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp))
                .constrainAs(bottomButton) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            if (transaction is Transaction.Unconfirmed) {
                TransactionDetailButton(content = "increase fees", navController = navController, txid = txid)
                Spacer(modifier = Modifier.padding(all = 8.dp))
            }
            TransactionDetailButton(
                content = "back to transaction list", navController = navController, txid = null)
        }
    }
}

@Composable
fun TransactionDetailButton(content: String, navController: NavController, txid: String?) {
    Button(
        onClick = {
            when (content) {
                "increase fees" -> {
                    navController.navigate("${Screen.RBFScreen.route}/txid=$txid")
                }
                "back to transaction list" -> {
                    navController.popBackStack()
                }
            }
        },
        colors = ButtonDefaults.buttonColors(DevkitWalletColors.frost4),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = content,
            fontSize = 14.sp,
            fontFamily = firaMono,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp,
        )
    }
}

fun getTransactionDetails(transaction: Transaction): List<Pair<String, String>> {
    val transactionDetails = mutableListOf<Pair<String, String>>()

    if (transaction is Transaction.Confirmed) {
        transactionDetails.add(Pair("Status", "Confirmed"))
        transactionDetails.add(Pair("Timestamp", transaction.confirmation.timestamp.timestampToString()))
        transactionDetails.add(Pair("Received", transaction.details.received.toString()))
        transactionDetails.add(Pair("Sent", transaction.details.sent.toString()))
        transactionDetails.add(Pair("Fees", transaction.details.fee.toString()))
        transactionDetails.add(Pair("Block", transaction.confirmation.height.toString()))
    } else if (transaction is Transaction.Unconfirmed) {
        transactionDetails.add(Pair("Status", "Pending"))
        transactionDetails.add(Pair("Timestamp", "Pending"))
        transactionDetails.add(Pair("Received", transaction.details.received.toString()))
        transactionDetails.add(Pair("Sent", transaction.details.sent.toString()))
        transactionDetails.add(Pair("Fees", transaction.details.fee.toString()))
    }
    return transactionDetails
}

fun transactionTitle(transaction: Transaction): String {
    if (transaction is Transaction.Confirmed) {
        return transaction.details.txid
    }
    return (transaction as Transaction.Unconfirmed).details.txid
}

fun getTransaction(txid: String?): Transaction? {
    if (txid.isNullOrEmpty()) {
        return null
    }
    return Wallet.getTransaction(txid = txid)
}