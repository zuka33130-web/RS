package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.ClientEntity
import com.example.data.model.ClientWithOrders
import com.example.data.model.DeviceModelCount
import com.example.data.model.FaultProfitStat
import com.example.data.model.WorkOrderEntity
import com.example.data.model.WorkOrderWithClient
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClients(clients: List<ClientEntity>)

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Delete
    suspend fun deleteClient(client: ClientEntity)

    @Query("SELECT * FROM crm_clients WHERE id = :id")
    suspend fun getClientById(id: Long): ClientEntity?

    @Query("SELECT * FROM crm_clients WHERE phone = :phone LIMIT 1")
    suspend fun findClientByPhone(phone: String): ClientEntity?

    @Query("SELECT * FROM crm_clients ORDER BY lastName ASC, firstName ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM crm_clients WHERE firstName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY lastName ASC")
    fun searchClients(query: String): Flow<List<ClientEntity>>

    @Transaction
    @Query("SELECT * FROM crm_clients ORDER BY lastName ASC")
    fun getClientsWithOrders(): Flow<List<ClientWithOrders>>

    @Query("SELECT COUNT(*) FROM crm_clients")
    suspend fun getClientCount(): Int

    @Query("DELETE FROM crm_clients")
    suspend fun deleteAllClients()
}

@Dao
interface WorkOrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: WorkOrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<WorkOrderEntity>)

    @Update
    suspend fun updateOrder(order: WorkOrderEntity)

    @Delete
    suspend fun deleteOrder(order: WorkOrderEntity)

    @Query("DELETE FROM work_orders WHERE id = :id")
    suspend fun deleteOrderById(id: Long)

    @Query("SELECT * FROM work_orders WHERE id = :id")
    suspend fun getOrderById(id: Long): WorkOrderEntity?

    @Query("SELECT * FROM work_orders ORDER BY entryDate DESC")
    fun getAllOrders(): Flow<List<WorkOrderEntity>>

    @Query("SELECT * FROM work_orders WHERE status = :status ORDER BY entryDate DESC")
    fun getOrdersByStatus(status: String): Flow<List<WorkOrderEntity>>

    @Transaction
    @Query("SELECT * FROM work_orders ORDER BY entryDate DESC")
    fun getAllOrdersWithClient(): Flow<List<WorkOrderWithClient>>

    @Transaction
    @Query("SELECT * FROM work_orders WHERE status = :status ORDER BY entryDate DESC")
    fun getOrdersWithClientByStatus(status: String): Flow<List<WorkOrderWithClient>>

    @Transaction
    @Query("SELECT * FROM work_orders WHERE id = :id")
    suspend fun getOrderWithClientById(id: Long): WorkOrderWithClient?

    @Query("SELECT deviceModel, COUNT(*) as count FROM work_orders GROUP BY deviceModel ORDER BY count DESC LIMIT 5")
    fun getTopDeviceModels(): Flow<List<DeviceModelCount>>

    @Query("SELECT reportedFault, COUNT(*) as count, SUM(totalCost - partsCost) as totalProfit FROM work_orders GROUP BY reportedFault ORDER BY totalProfit DESC LIMIT 5")
    fun getTopProfitFaults(): Flow<List<FaultProfitStat>>

    @Query("SELECT COUNT(*) FROM work_orders")
    suspend fun getOrderCount(): Int

    @Query("SELECT * FROM work_orders")
    suspend fun getAllOrdersList(): List<WorkOrderEntity>

    @Query("DELETE FROM work_orders")
    suspend fun deleteAllOrders()
}
