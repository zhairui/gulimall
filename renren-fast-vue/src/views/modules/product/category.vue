<!--  -->
<template>
  <div>
    <el-switch v-model="draggable" active-text="开启拖拽" inactive-text="关闭拖拽"></el-switch>
    <el-button v-if="draggable"  @click="batchSave">批量保存</el-button>

    <el-tree
      :data="menus"
      show-checkbox
      :props="defaultProps"
      :expand-on-click-node="false"
      node-key="catId"
      :default-expanded-keys="expendedkey"
      :draggable="draggable"
      :allow-drop="allowDrop"
      @node-drop="handleDrop"
    >
      <span class="custom-tree-node" slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
        <span>
          <el-button
            v-if="node.level <= 2"
            type="text"
            size="mini"
            @click="() => append(data)"
          >Append</el-button>

          <el-button type="text" size="mini" @click="edit(data)">Edit</el-button>

          <el-button
            v-if="node.childNodes.length == 0"
            type="text"
            size="mini"
            @click="() => remove(node, data)"
          >Delete</el-button>
        </span>
      </span>
    </el-tree>

    <el-dialog
      :title="title"
      :visible.sync="dialogVisible"
      width="30%"
      :close-on-click-modal="false"
    >
      <el-form :model="category">
        <el-form-item label="分类名称">
          <el-input v-model="category.name"></el-input>
        </el-form-item>
      </el-form>

      <el-form :model="category">
        <el-form-item label="图标">
          <el-input v-model="category.icon"></el-input>
        </el-form-item>
      </el-form>

      <el-form :model="category">
        <el-form-item label="计量单位">
          <el-input v-model="category.productUnit"></el-input>
        </el-form-item>
      </el-form>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitData">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
//这里可以导入其他文件（比如：组件，工具js，第三方插件js，json文件，图片文件等等）
//例如：import 《组件名称》 from '《组件路径》';

export default {
  //import引入的组件需要注入到对象中才能使用
  components: {},
  props: {},

  data() {
    //这里存放数据
    return {
      pCid: [],
      draggable: false,
      updateNodes: [],
      maxLevel: 0,
      title: "",
      dialogType: "", //edit和append的标识
      category: {
        name: "",
        parentCid: 0,
        catLevel: 0,
        showStatus: 1,
        sort: 0,
        catId: 0,
        icon: "",
        productUnit: ""
      },
      dialogVisible: false,
      menus: [],
      expendedkey: [],
      defaultProps: {
        children: "children",
        label: "name"
      }
    };
  },
  // 监听属性 类似于data概念
  computed: {},
  // 监控data中的数据变化
  watch: {},
  // 方法集合
  methods: {
    //批量保存
    batchSave() {
      this.$http({
        url: this.$http.adornUrl("/product/category/update/sort"),
        method: "post",
        data: this.$http.adornData(this.updateNodes, false)
      }).then(({ data }) => {
        //修改成功后，给出提示信息

        this.$message({
          message: "菜单顺序修改成功",
          type: "success"
        });

        //重新刷新页面
        this.getMeus();

        //设置默认需要展开的菜单
        this.expendedkey = this.pCid;
        //恢复默认值
        this.updateNodes = [];
        this.maxLevel = 0;
        
      });
    },

    //拖拽成功后的事件
    handleDrop(draggingNode, dropNode, dropType, ev) {
      console.log("tree drop: ", draggingNode, dropNode, dropType);

       
      let siblings = null;
      let pCid = 0;
      //根据拖拽类型，获取当前节点的父节点的id
      if (dropType == "before" || dropType == "after") {
         pCid = dropNode.data.parentCid;

        siblings = dropNode.parent.childNodes;
      } else {
          pCid = dropNode.data.catId;
        siblings = dropNode.childNodes;
      }

        this.pCid.push(pCid);

      console.log("siblings:", siblings);
      //重新排序拖拽后的数据
      for (let i = 0; i < siblings.length; i++) {
        /**
         * 这里的处理逻辑是，兄弟节点数组中非当前拖拽节点(draggingNode)节点,设置它们的排列顺序为遍历顺序
         * 对于兄弟节点数组中的当前拖拽节点(draggingNode)节点，根据拖拽类型来设置catLevel，parentCid和sort
         * 1. 如果拖拽类型为inner，则设置它的parentCid为dropNode的cid，catLevel为dropNode的catLevel+1，顺序为遍历顺序
         * 2. 如果拖拽类型是before或after，则比较当前拖拽节点的Level和dropNode的Level
         * 2.1 比较结果不同，说明它们并不是在同一个层级中，此时需要设置它的sort，parentCid和catLevel，后面的catLevel取自于dropNode
         * 2.2 比较结果相同，说明当前拖拽节点和dropNode处于同一层级下，此时如果它们都是在同一个父节点下，则只需要重新设置sort即可，而如果并不在同一个父节点下
         * 这种情况下，就需要设置parentCid和sort了。
         */
        if (siblings[i].data.catId == draggingNode.data.catId) {
          //更新被拖拽节点的catLevel
          if (dropType == "before" || dropType == "after") {
            if (siblings[i].data.catLevel == dropNode.data.catLevel) {
              if (siblings[i].data.parentCid == dropNode.data.parentCid) {
                this.updateNodes.push({
                  catId: siblings[i].data.catId,
                  sort: i
                });
              } else {
                this.updateNodes.push({
                  catId: siblings[i].data.catId,
                  sort: i,
                  parentCid: pCid
                });
              }
            } else {
              this.updateNodes.push({
                catId: siblings[i].data.catId,
                sort: i,
                parentCid: pCid,
                catLevel: dropNode.data.catLevel
              });
              //如果被拖拽节点存在子节点，则更新子节点的catLevel
              this.updateChildNodeLevel(siblings[i]);
            }
          } else {
            this.updateNodes.push({
              catId: siblings[i].data.catId,
              sort: i,
              parentCid: pCid,
              catLevel: dropNode.data.catLevel + 1
            });
            //如果被拖拽节点存在子节点，则更新子节点的catLevel
            this.updateChildNodeLevel(siblings[i]);
          }

          //  //如果拖拽节点的层级关系和目标节点的层级关系不同（如发生在拖动到了和父节点平级或拖动到了兄弟节点中了）
          //  if(siblings[i].data.catLevel != dropNode.data.catLevel){
          //        //如果是拖动到目标节点的中了，则层级关系需要加1
          //        if(dropType == "inner"){
          //            this.updateNodes.push({catId:siblings[i].data.catId,sort:i,parentCid: pCid,catLevel:(dropNode.data.catLevel+1)});
          //        }else{
          //          //如果是拖动到了目标节点之前或之后，则层级设置为目标节点的层级
          //            this.updateNodes.push({catId:siblings[i].data.catId,sort:i,parentCid: pCid,catLevel:dropNode.data.catLevel});
          //        }

          //  }else{
          //        this.updateNodes.push({catId:siblings[i].data.catId,sort:i,parentCid: pCid});
          //  }
        } else {
          this.updateNodes.push({ catId: siblings[i].data.catId, sort: i });
        }
      }

      console.log("updateNodes", this.updateNodes);
    },

    //更新子节点的catLevel
    updateChildNodeLevel(node) {
      if (node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          var cNode = node.childNodes[i].data;
          this.updateNodes.push({
            catId: cNode.catId,
            catLevel: node.childNodes[i].level
          });
          this.updateChildNodeLevel(node.childNodes[i]);
        }
      }
    },

    //菜单拖拽功能
    allowDrop(draggingNode, dropNode, type) {
      console.log("dragDrop", draggingNode, dropNode, type);

      this.countNodeLevel(draggingNode);

      console.log("当前节点的level", this.maxLevel);

      //当前节点的深度（以当前节点为基准,没有子节点，则deep为0）
      let deep = Math.abs(this.maxLevel - draggingNode.level);

      console.log("当前节点的deep", deep);
      //判断是否可以拖动节点，当且仅当拖动后目标节点的层级小于3时，才允许拖动

      if (type == "inner") {
        return dropNode.level + deep < 3;
      } else {
        return dropNode.level + deep <= 3;
      }
    },

    countNodeLevel(currentNode) {
      let childNodes = currentNode.childNodes;
      if (childNodes != null && childNodes.length > 0) {
        for (let i = 0; i < childNodes.length; i++) {
          if (childNodes[i].level > this.maxLevel) {
            this.maxLevel = childNodes[i].level;
          }
          this.countNodeLevel(childNodes[i]);
        }
      } else {
        this.maxLevel = currentNode.level;
      }
    },

    submitData() {
      if (this.dialogType == "add") {
        this.addCategory();
      }
      if (this.dialogType == "edit") {
        this.editCategory();
      }
    },

    editCategory() {
      var { name, catId, icon, productUnit } = this.category;
      this.$http({
        url: this.$http.adornUrl("/product/category/update"),
        method: "post",
        data: this.$http.adornData({ name, catId, icon, productUnit }, false)
      }).then(({ data }) => {
        this.$message({
          message: "菜单修改成功",
          type: "success"
        });

        //关闭对话框
        this.dialogVisible = false;

        //重新刷新页面
        this.getMeus();

        //设置默认需要展开的菜单
        this.expendedkey = [this.category.parentCid];
      });
    },

    addCategory() {
      console.log("提交三级分类数据", this.category);
      this.$http({
        url: this.$http.adornUrl("/product/category/save"),
        method: "post",
        data: this.$http.adornData(this.category, false)
      }).then(({ data }) => {
        this.$message({
          message: "菜单保存成功",
          type: "success"
        });

        //关闭对话框
        this.dialogVisible = false;

        //重新刷新页面
        this.getMeus();

        //设置默认需要展开的菜单
        this.expendedkey = [this.category.parentCid];
      });
    },
    handleNodeClick(data) {
      console.log(data);
    },
    getMeus() {
      this.$http({
        url: this.$http.adornUrl("/product/category/list/tree"),
        method: "get"
      }).then(({ data }) => {
        // console.log(data,data);
        this.menus = data;
      });
    },

    edit(data) {
      this.title = "修改分类";
      this.dialogType = "edit";
      console.log("要修改的数据", data);
      this.dialogVisible = true;
      //重新请求数据进行回显
      this.$http({
        url: this.$http.adornUrl(`/product/category/info/${data.catId}`),
        method: "get"
      }).then(({ data }) => {
        console.log("要回显的数据", data);
        this.category.name = data.data.name;
        this.category.catId = data.data.catId;
        this.category.icon = data.data.icon;
        this.category.productUnit = data.data.productUnit;
        this.category.parentCid = data.data.parentCid;
      });
    },

    append(data) {
      this.title = "添加分类";
      this.dialogType = "add";
      this.dialogVisible = true;
      this.category.parentCid = data.catId;
      this.category.catLevel = data.catLevel * 1 + 1;

      //复位数据
      this.category.name = "";
      this.category.catId = 0;
      this.category.icon = "";
      this.category.productUnit = "";

      console.log("data", data);
    },

    remove(node, data) {
      var ids = [data.catId];

      this.$confirm(`是否删除【${data.name}】菜单?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      })
        .then(() => {
          //执行删除逻辑
          this.$http({
            url: this.$http.adornUrl("/product/category/delete"),
            method: "post",
            data: this.$http.adornData(ids, false)
          }).then(({ data }) => {
            //删除成功
            this.$message({
              message: "菜单删除成功",
              type: "success"
            });

            //重新刷新页面
            this.getMeus();

            //设置默认需要展开的菜单
            this.expendedkey = [node.data.parentCid];
          });
        })
        .catch(() => {
          //取消删除
        });

      console.log("remove", node, data);
    }
  },
  // 生命周期 - 创建完成（可以访问当前this实例）
  created() {
    this.getMeus();
  },
  // 生命周期 - 挂载完成（可以访问DOM元素）
  mounted() {},
  beforeCreate() {}, //生命周期 - 创建之前
  beforeMount() {}, //生命周期 - 挂载之前
  beforeUpdate() {}, //生命周期 - 更新之前
  updated() {}, //生命周期 - 更新之后
  beforeDestroy() {}, //生命周期 - 销毁之前
  destroyed() {}, //生命周期 - 销毁完成
  activated() {} //如果页面有keep-alive缓存功能，这个函数会触发
};
</script>
<style lang='scss' scoped>
//@import url(); 引入公共css类
</style>