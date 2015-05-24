package org.ultramine.server.asm.transformers;

import java.util.ListIterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.launchwrapper.IClassTransformer;

public class TrigMathTransformer implements IClassTransformer
{
	private static final Logger log = LogManager.getLogger();
	
	private static final String TRIGMATH_TYPE = "org/ultramine/server/util/TrigMath";
	private static final String MATH_TYPE = "java/lang/Math";
	private static final String ATAN2_NAME = "atan2";
	private static final String ATAN2_DESC = "(DD)D";
	private static final String ATAN_NAME = "atan";
	private static final String ATAN_DESC = "(D)D";
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if (basicClass == null)
			return null;
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(basicClass);
		classReader.accept(classNode, 0);

		for (MethodNode m: classNode.methods)
		{
			for (ListIterator<AbstractInsnNode> it = m.instructions.iterator(); it.hasNext(); )
			{
				AbstractInsnNode insnNode = it.next();
				if (insnNode.getType() == AbstractInsnNode.METHOD_INSN)
				{
					MethodInsnNode fi = (MethodInsnNode)insnNode;
					if (MATH_TYPE.equals(fi.owner) && ATAN2_NAME.equals(fi.name) && ATAN2_DESC.equals(fi.desc) && fi.getOpcode() == Opcodes.INVOKESTATIC)
					{
						log.trace("Method {}.{}{}: Replacing INVOKESTATIC Math.atan2 with INVOKESTATIC TrigMath.atan2", name, m.name, m.desc);
						it.remove();
						MethodInsnNode replace = new MethodInsnNode(Opcodes.INVOKESTATIC, TRIGMATH_TYPE, ATAN2_NAME, ATAN2_DESC, false);
						it.add(replace);
					}
					
					if (MATH_TYPE.equals(fi.owner) && ATAN_NAME.equals(fi.name) && ATAN_DESC.equals(fi.desc) && fi.getOpcode() == Opcodes.INVOKESTATIC)
					{
						log.trace("Method {}.{}{}: Replacing INVOKESTATIC Math.atan with INVOKESTATIC TrigMath.atan", name, m.name, m.desc);
						it.remove();
						MethodInsnNode replace = new MethodInsnNode(Opcodes.INVOKESTATIC, TRIGMATH_TYPE, ATAN_NAME, ATAN_DESC, false);
						it.add(replace);
					}
				}
			}
		}
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}
}
