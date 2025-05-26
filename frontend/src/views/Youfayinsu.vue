<template>
    <div>
        <el-table
                :data="tableData"
                border
                style="width: 100%">
            <el-table-column
                    fixed
                    prop="aircraftstatus"
                    label="飞行阶段"
                    width="150">
            </el-table-column>
            <el-table-column
                    prop="faultPhenomenon"
                    label="故障现象"
                    width="120">
            </el-table-column>
            <el-table-column
                    prop="faultLocation"
                    label="故障部位"
                    width="120">
            </el-table-column>
            <el-table-column
                    prop="treatrentheasures"
                    label="处理措施"
                    width="300">
            </el-table-column>
            <el-table-column
                    prop="faultoiagnosis"
                    label="故障诊断"
                    width="300">
            </el-table-column>
            <el-table-column
                    prop="faultType"
                    label="故障类型"
                    width="120">
            </el-table-column>

            <el-table-column
                    fixed="right"
                    label="操作"
                    width="180">
                <template slot-scope="scope">
                    <el-button @click="handleClick(scope.row)" type="text" size="small">查看</el-button>
                    <el-button @click="editRow(scope.row)" type="text" size="small">编辑</el-button>
                </template>
            </el-table-column>
        </el-table>

        <el-pagination
                style="margin-top: 20px"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :total="total"
                :current-page="currentPage"
                :page-sizes="[5, 10, 20]"
                :page-size="pageSize"
                layout="total, sizes, prev, pager, next">
        </el-pagination>
        <el-dialog :visible.sync="dialogVisible" title="编辑信息">
            <el-form :model="editForm">
                <el-form-item label="日期">
                    <el-input v-model="editForm.date"></el-input>
                </el-form-item>
                <el-form-item label="姓名">
                    <el-input v-model="editForm.name"></el-input>
                </el-form-item>
                <el-form-item label="省份">
                    <el-input v-model="editForm.province"></el-input>
                </el-form-item>
                <el-form-item label="市区">
                    <el-input v-model="editForm.city"></el-input>
                </el-form-item>
                <el-form-item label="地址">
                    <el-input v-model="editForm.address"></el-input>
                </el-form-item>
                <el-form-item label="邮编">
                    <el-input v-model="editForm.zip"></el-input>
                </el-form-item>
            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="saveEdit">提交</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
    import { graphService } from '@/service/graph';

    export default {
        data() {
            return {
                tableData: [],
                total: 0,
                currentPage: 1,
                pageSize: 5,
                dialogVisible: false,
                editForm: {
                    date: '',
                    name: '',
                    province: '',
                    city: '',
                    address: '',
                    zip: ''
                }
            };
        },
        created() {
            // this.fetchData();
        },
        methods: {
            fetchData() {
                const params = {
                    page: this.currentPage,
                    pageSize: this.pageSize
                };
                graphService.getInduceFactors(params)
                    .then(response => {
                        this.tableData = response.result;
                        this.total = response.total;
                    })
                    .catch(error => {
                        console.error('获取诱发因素数据失败:', error);
                    });
            },
            editRow(row) {
                this.editForm = { ...row };
                this.dialogVisible = true;
            },
            saveEdit() {
                graphService.updateInduceFactor(this.editForm)
                    .then(response => {
                        if (response.code === 200) {
                            this.$message({
                                type: "success",
                                message: "保存成功"
                            });
                            this.dialogVisible = false;
                            this.fetchData();
                        } else {
                            this.$message({
                                type: "error",
                                message: "保存失败"
                            });
                        }
                    })
                    .catch(error => {
                        console.error('保存诱发因素数据失败:', error);
                        this.$message({
                            type: "error",
                            message: "保存失败"
                        });
                    });
            },
            handleSizeChange(val) {
                this.pageSize = val;
                this.handleCurrentChange(1);
            },
            handleCurrentChange(val) {
                this.currentPage = val;
                this.fetchData();
            },
            handleClick(row) {
                console.log(row);
            }
        }
    };
</script>

<style scoped>

</style>